package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole;
import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.backend.permissions.BundlePermissionEvaluator;
import io.github.sanyavertolet.edukate.backend.repositories.BundleRepository;
import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.services.HttpNotifierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BundleService {
    private final BundleRepository bundleRepository;
    private final ShareCodeGenerator shareCodeGenerator;
    private final SubmissionService submissionService;
    private final ProblemService problemService;
    private final BundlePermissionEvaluator bundlePermissionEvaluator;
    private final HttpNotifierService notifierService;

    public Mono<Bundle> findBundleByShareCode(String shareCode) {
        return bundleRepository.findBundleByShareCode(shareCode).switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Bundle [" + shareCode + "] not found")
        ));
    }

    public Flux<Bundle> getOwnedBundles(Authentication authentication, PageRequest pageable) {
        return Mono.justOrEmpty(authentication).flatMapMany(auth ->
                bundleRepository.findBundlesByUserRoleIn(auth.getName(), List.of(Role.ADMIN), pageable)
        );
    }

    public Flux<Bundle> getJoinedBundles(Authentication authentication, PageRequest pageable) {
        return Mono.justOrEmpty(authentication).flatMapMany(auth ->
                bundleRepository.findBundlesByUserRoleIn(auth.getName(), Role.anyRole(), pageable)
        );
    }

    public Flux<Bundle> getPublicBundles(PageRequest pageable) {
        return bundleRepository.findBundlesByIsPublic(true, pageable);
    }

    public Mono<BundleDto> prepareDto(Bundle bundle, Authentication authentication) {
        return Mono.justOrEmpty(bundle)
                .map(Bundle::toDto)
                .zipWhen(_ ->
                        problemService.findProblemListByIds(bundle.getProblemIds())
                                .flatMapMany(problemMetadataList ->
                                        submissionService.updateStatusInMetadataMany(authentication, problemMetadataList))
                                .collectList()
                )
                .map(tuple -> {
                    BundleDto dto = tuple.getT1();
                    List<ProblemMetadata> metadataList = tuple.getT2();

                    return dto.withProblems(metadataList);
                });
    }

    public Mono<Bundle> createBundle(CreateBundleRequest createBundleRequest, Authentication authentication) {
        return Mono.just(createBundleRequest)
                .map(request -> Bundle.fromCreateRequest(request, authentication.getName()))
                .map(bundle -> bundle.updateShareCode(shareCodeGenerator.generateShareCode()))
                .flatMap(bundleRepository::save);
    }

    @Transactional
    public Mono<Bundle> joinUser(String userId, String shareCode) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundlePermissionEvaluator.hasJoinPermission(bundle, userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "To join this bundle, you should be invited"
                )))
                .filter(bundle -> !bundle.isUserInBundle(userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "You have already joined the bundle [" + shareCode + "]"
                )))
                .map(bundle -> {
                    bundle.addUser(userId, Role.USER);
                    return bundle;
                })
                .flatMap(bundleRepository::save);
    }

    @Transactional
    public Mono<Bundle> removeUser(String userId, String shareCode) {
        return findBundleByShareCode(shareCode)
                .filter(bundle ->
                        bundle.isUserInBundle(userId)
                )
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in bundle")
                ))
                .filter(bundle ->
                        !bundle.isAdmin(userId) || bundle.getAdmins().size() > 1
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Last admin should delete bundle, not leave it"
                )))
                .map(bundle -> {
                    bundle.removeUser(userId);
                    return bundle;
                })
                .flatMap(bundleRepository::save);
    }

    @Transactional
    public Mono<String> inviteUser(String inviterId, String inviteeId, String shareCode) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundlePermissionEvaluator.hasInvitePermission(bundle, inviterId))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permissions to invite")
                ))
                .filter(bundle -> !bundle.isUserInBundle(inviteeId))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in bundle")
                ))
                .filter(bundle -> !bundle.isUserInvited(inviteeId))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has already been invited")
                ))
                .flatMap(bundle -> {
                    if (bundle.inviteUser(inviteeId) == 0) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Cannot invite user " + inviteeId + " to bundle " + shareCode + " due to internal error")
                        );
                    }
                    bundle.removeInvitedUser(inviteeId);
                    return Mono.just(bundle);
                })
                .flatMap(bundleRepository::save)
                .flatMap(bundle ->
                        notifierService.notifyInvite(inviteeId, inviterId, bundle.getName(), bundle.getShareCode())
                );
    }

    public Mono<Map<String, Role>> getBundleUsers(String shareCode, Authentication authentication) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundlePermissionEvaluator.hasInvitePermission(bundle, authentication.getName()))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permissions...")
                ))
                .map(Bundle::getUserRoles);
    }

    public Mono<List<UserNameWithRole>> mapToList(Map<String, Role> userRoles) {
        return Mono.justOrEmpty(userRoles)
                .map(map ->
                        map.entrySet().stream().map(mapEntry ->
                                new UserNameWithRole(mapEntry.getKey(), mapEntry.getValue()))
                                .toList()
                );
    }

    @Transactional
    public Mono<Role> changeUserRole(String shareCode, String username, Role requestedRole, Authentication authentication) {
        return findBundleByShareCode(shareCode)
                .filter(bundle ->
                        bundlePermissionEvaluator.hasChangeRolePermission(
                                bundle, authentication.getName(), username, requestedRole
                        )
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Cannot set " + username + " role to be " + requestedRole
                )))
                .map(bundle -> {
                    bundle.changeUserRole(username, requestedRole);
                    return bundle;
                })
                .flatMap(bundleRepository::save)
                .thenReturn(requestedRole);
    }

    @Transactional
    public Mono<Bundle> declineInvite(String shareCode, Authentication authentication) {
        return findBundleByShareCode(shareCode)
                .filter(bundle -> bundle.isUserInvited(authentication.getName()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "You cannot decline the invitation as nobody has invited you yet"
                )))
                .flatMap(bundle -> {
                    if (bundle.removeInvitedUser(authentication.getName()) == 0) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Cannot decline invitation for user " + authentication.getName() + " due to internal error"
                        ));
                    }
                    return Mono.just(bundle);
                })
                .flatMap(bundleRepository::save);
    }
}
