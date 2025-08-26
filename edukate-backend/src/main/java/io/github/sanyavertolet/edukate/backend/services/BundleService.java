package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata;
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest;
import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.backend.permissions.BundlePermissionEvaluator;
import io.github.sanyavertolet.edukate.backend.repositories.BundleRepository;
import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.entities.User;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BundleService {
    private final BundleRepository bundleRepository;
    private final ShareCodeGenerator shareCodeGenerator;
    private final ProblemService problemService;
    private final BundlePermissionEvaluator bundlePermissionEvaluator;
    private final UserService userService;

    public Mono<Bundle> findBundleByShareCode(String shareCode) {
        return bundleRepository.findBundleByShareCode(shareCode).switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Bundle [" + shareCode + "] not found")
        ));
    }

    public Flux<Bundle> getOwnedBundles(PageRequest pageable, Authentication authentication) {
        return AuthUtils.monoId(authentication).flatMapMany(userId ->
                bundleRepository.findBundlesByUserRoleIn(userId, List.of(Role.ADMIN), pageable)
        );
    }

    public Flux<Bundle> getJoinedBundles(PageRequest pageable, Authentication authentication) {
        return AuthUtils.monoId(authentication).flatMapMany(userId ->
                bundleRepository.findBundlesByUserRoleIn(userId, Role.anyRole(), pageable)
        );
    }

    public Flux<Bundle> getPublicBundles(PageRequest pageable) {
        return bundleRepository.findBundlesByIsPublic(true, pageable);
    }

    public Mono<Bundle> createBundle(CreateBundleRequest createBundleRequest, Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .map(userId -> Bundle.fromCreateRequest(createBundleRequest, userId))
                .map(bundle -> bundle.withShareCode(shareCodeGenerator.generateShareCode()))
                .flatMap(bundleRepository::save);
    }

    @Transactional
    public Mono<Bundle> joinUser(String shareCode, String userId) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundlePermissionEvaluator.hasJoinPermission(bundle, userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "To join this bundle, you should be invited"
                )))
                .filter(bundle -> !bundle.isUserInBundle(userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "You have already joined the bundle [" + shareCode + "]"
                )))
                .map(bundle -> {
                    bundle.addUser(userId, Role.USER);
                    bundle.removeInvitedUser(userId);
                    return bundle;
                })
                .flatMap(bundleRepository::save);
    }

    @Transactional
    public Mono<Bundle> removeUser(String shareCode, String userId) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundle.isUserInBundle(userId))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in bundle")
                ))
                .filter(bundle -> !bundle.isAdmin(userId) || bundle.getAdminIds().size() > 1)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Last admin should delete bundle, not leave it"
                )))
                .doOnNext(bundle -> bundle.removeUser(userId))
                .flatMap(bundleRepository::save);
    }

    @Transactional
    public Mono<Bundle> inviteUser(String shareCode, String inviterId, String inviteeId) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundlePermissionEvaluator.hasInvitePermission(bundle, inviterId))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permissions to invite")
                ))
                .filter(bundle -> !bundle.isUserInBundle(inviteeId))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in bundle")
                ))
                .doOnNext(bundle -> bundle.inviteUser(inviteeId))
                .flatMap(bundleRepository::save);

    }

    public Flux<Map.Entry<String, Role>> getBundleUserIdsWithRoles(String shareCode, Authentication authentication) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundlePermissionEvaluator.hasRole(bundle, AuthUtils.id(authentication), Role.MODERATOR))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")))
                .map(Bundle::getUserIdRoleMap)
                .flatMapMany(map -> Flux.fromIterable(map.entrySet()));
    }

    @Transactional
    public Mono<Role> changeUserRole(String shareCode, String userId, Role requestedRole, Authentication authentication) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundle.isUserInBundle(userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Target user not found in bundle"
                )))
                .filter(bundle -> bundlePermissionEvaluator.hasChangeRolePermission(
                        bundle, AuthUtils.id(authentication), userId, requestedRole
                ))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Cannot set " + userId + " role to be " + requestedRole
                )))
                .doOnNext(bundle -> bundle.changeUserRole(userId, requestedRole))
                .flatMap(bundleRepository::save)
                .thenReturn(requestedRole);
    }

    @Transactional
    public Mono<Bundle> declineInvite(String shareCode, Authentication authentication) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundle.isUserInvited(AuthUtils.id(authentication)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You cannot decline the invitation as nobody has invited you yet"
                )))
                .flatMap(bundle -> {
                    if (!bundle.removeInvitedUser(AuthUtils.id(authentication))) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Cannot decline invitation for user " + authentication.getName() + " due to internal error"
                        ));
                    }
                    return Mono.just(bundle);
                })
                .flatMap(bundleRepository::save);
    }

    @Transactional
    public Mono<Bundle> changeVisibility(String shareCode, boolean isPublic, Authentication authentication) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundlePermissionEvaluator.hasRole(bundle, Role.MODERATOR, authentication))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Cannot change visibility due to lack of permissions"
                )))
                .doOnNext(bundle -> bundle.setIsPublic(isPublic))
                .flatMap(bundleRepository::save);
    }

    @Transactional
    public Mono<Bundle> changeProblems(String shareCode, List<String> problemIds, Authentication authentication) {
        return Mono.just(shareCode)
                .filter(_ -> problemIds != null && !problemIds.isEmpty())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Bundle problem list cannot be empty"
                )))
                .flatMap(this::findBundleByShareCode)
                .filter(bundle -> bundlePermissionEvaluator.hasRole(bundle, Role.MODERATOR, authentication))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Cannot change problem list due to lack of permissions"
                )))
                .doOnNext(bundle -> bundle.setProblemIds(new ArrayList<>(problemIds)))
                .flatMap(bundleRepository::save);
    }

    public Mono<BundleDto> prepareDto(Bundle bundle, Authentication authentication) {
        return problemService.findProblemsByIds(bundle.getProblemIds())
                .flatMap(problem -> problemService.prepareMetadata(problem, authentication))
                .collectList()
                .map(list -> bundle.toDto().withProblems(list))
                .flatMap(dto -> getAdmins(bundle).map(dto::withAdmins));
    }

    public Mono<BundleMetadata> prepareMetadata(Bundle bundle) {
        return getAdmins(bundle).map(admins -> bundle.toBundleMetadata().withAdmins(admins));
    }

    private Mono<List<String>> getAdmins(Bundle bundle) {
        return Mono.just(bundle.getAdminIds())
                .flatMapMany(userService::findUsersByIds)
                .map(User::getName)
                .collectList();
    }
}
