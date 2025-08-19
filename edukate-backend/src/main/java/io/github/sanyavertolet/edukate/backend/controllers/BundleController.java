package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.*;
import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.backend.services.BundleService;
import io.github.sanyavertolet.edukate.backend.services.UserService;
import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.entities.User;
import io.github.sanyavertolet.edukate.common.services.HttpNotifierService;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/bundles")
@Tag(name = "Bundles", description = "API for managing problem bundles and user collaborations")
public class BundleController {
    private final BundleService bundleService;
    private final UserService userService;
    private final HttpNotifierService notifierService;

    @PostMapping
    @Operation(
            summary = "Create a bundle",
            description = "Creates a new bundle with the specified properties"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created bundle",
                    content = @Content(schema = @Schema(implementation = BundleDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateBundleRequest.class)
            )
    )
    public Mono<BundleDto> createBundle(@RequestBody @Valid CreateBundleRequest request, Authentication authentication) {
        return bundleService.createBundle(request, authentication)
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }

    @GetMapping("/owned")
    @Operation(
            summary = "Get owned bundles",
            description = "Retrieves a paginated list of bundles owned by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved owned bundles",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BundleMetadata.class)))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)", in = QUERY,
                    schema = @Schema(minimum = "0")),
            @Parameter(name = "size", description = "Number of bundles per page", in = QUERY,
                    schema = @Schema(minimum = "1", maximum = "100")),
    })
    public Flux<BundleMetadata> getOwnedBundles(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size,
            Authentication authentication
    ) {
        return bundleService.getOwnedBundles(PageRequest.of(page, size), authentication)
                .flatMap(bundleService::prepareMetadata);
    }

    @GetMapping("/joined")
    @Operation(
            summary = "Get joined bundles",
            description = "Retrieves a paginated list of bundles joined by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved joined bundles",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BundleMetadata.class)))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)", in = QUERY,
                    schema = @Schema(minimum = "0")),
            @Parameter(name = "size", description = "Number of bundles per page", in = QUERY,
                    schema = @Schema(minimum = "1", maximum = "100")),
    })
    public Flux<BundleMetadata> getJoinedBundles(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size,
            Authentication authentication
    ) {
        return bundleService.getJoinedBundles(PageRequest.of(page, size), authentication)
                .flatMap(bundleService::prepareMetadata);
    }

    @GetMapping("/public")
    @Operation(
            summary = "Get public bundles",
            description = "Retrieves a paginated list of public bundles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved public bundles",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BundleMetadata.class)))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)", in = QUERY,
                    schema = @Schema(minimum = "0")),
            @Parameter(name = "size", description = "Number of bundles per page", in = QUERY,
                    schema = @Schema(minimum = "1", maximum = "100")),
    })
    public Flux<BundleMetadata> getPublicBundles(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return bundleService.getPublicBundles(PageRequest.of(page, size))
                .flatMap(bundleService::prepareMetadata);
    }

    @GetMapping("/{shareCode}")
    @Operation(
            summary = "Get bundle by share code",
            description = "Retrieves a bundle by its share code (user must be a member of the bundle)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bundle",
                    content = @Content(schema = @Schema(implementation = BundleDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - User is not a member of the bundle",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
    })
    public Mono<BundleDto> getBundleByShareCode(
            @PathVariable @NotBlank String shareCode,
            Authentication authentication) {
        return bundleService.findBundleByShareCode(shareCode)
                .filter(bundle -> bundle.isUserInBundle(AuthUtils.id(authentication)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You must be a member of the bundle to view it."
                )))
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }

    @PostMapping("/{shareCode}/join")
    @Operation(
            summary = "Join a bundle",
            description = "Joins a bundle using its share code"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined bundle",
                    content = @Content(schema = @Schema(implementation = BundleMetadata.class))),
            @ApiResponse(responseCode = "400", description = "Already in bundle", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - invite is required to join",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
    })
    public Mono<BundleMetadata> joinBundle(
            @PathVariable @NotBlank String shareCode,
            Authentication authentication
    ) {
        return AuthUtils.monoId(authentication)
                .flatMap(userId -> bundleService.joinUser(shareCode, userId))
                .flatMap(bundleService::prepareMetadata);
    }

    @PostMapping("/{shareCode}/leave")
    @Operation(
            summary = "Leave a bundle",
            description = "Leaves a bundle the user is currently a member of and returns bundle shareCode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully left bundle",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Either not a participant or last admin",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
    })
    public Mono<String> leaveBundle(
            @PathVariable @NotBlank String shareCode,
            Authentication authentication
    ) {
        return AuthUtils.monoId(authentication)
                .flatMap(userId -> bundleService.removeUser(shareCode, userId))
                .map(Bundle::getShareCode);
    }

    @PostMapping("/{shareCode}/invite")
    @Operation(
            summary = "Invite user to bundle",
            description = "Invites another user to join a bundle"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully invited user",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "User is already in bundle", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - Insufficient permissions in bundle",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle or user not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
            @Parameter(name = "inviteeName", description = "Username of the user to invite", in = QUERY,
                    required = true),
    })
    public Mono<String> inviteToBundle(
            @PathVariable @NotBlank String shareCode,
            @RequestParam @NotBlank String inviteeName,
            Authentication authentication
    ) {
        return userService.findUserByName(inviteeName)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User " + inviteeName +  " not found"
                )))
                .zipWhen(invitee -> bundleService.inviteUser(shareCode, AuthUtils.id(authentication), invitee.getId()))
                .flatMap(tuple -> {
                    User invitee = tuple.getT1();
                    Bundle bundle = tuple.getT2();
                    return notifierService.notifyInvite(
                            invitee.getId(), authentication.getName(), bundle.getName(), bundle.getShareCode()
                    )
                            .thenReturn("User " + inviteeName + " has been invited to bundle " + shareCode);
                });
    }

    @PostMapping("/{shareCode}/reply-invite")
    @Operation(
            summary = "Reply to bundle invitation",
            description = "Accepts or declines an invitation to join a bundle"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully replied to invitation",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "User is already in bundle", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - User is not invited", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
            @Parameter(name = "response", description = "True to accept, false to decline", in = QUERY,
                    required = true),
    })
    public Mono<String> replyToInvite(
            @PathVariable @NotBlank String shareCode,
            @RequestParam @NotNull Boolean response,
            Authentication authentication
    ) {
        return Mono.just(response).flatMap(hasAccepted -> {
            if (hasAccepted) {
                return bundleService.joinUser(shareCode, AuthUtils.id(authentication))
                        .thenReturn("You have accepted invite to bundle " + shareCode);
            }
            return bundleService.declineInvite(shareCode, authentication)
                    .thenReturn("You have declined invite to bundle " + shareCode);
            });
    }

    @GetMapping("/{shareCode}/users")
    @Operation(
            summary = "Get users in bundle",
            description = "Retrieves a list of users in a bundle with their roles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserNameWithRole.class)))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
    })
    public Mono<List<UserNameWithRole>> getUserRoles(
            @PathVariable @NotBlank String shareCode,
            Authentication authentication
    ) {
        return bundleService.getBundleUserIdsWithRoles(shareCode, authentication)
                .flatMap(entry -> userService.findUserById(entry.getKey())
                        .map(User::getName)
                        .map(userName -> new UserNameWithRole(userName, entry.getValue()))
                )
                .collectList();
    }

    @PostMapping("/{shareCode}/role")
    @Operation(
            summary = "Change user role in bundle",
            description = "Changes user role in a bundle by their name"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed user role",
                    content = @Content(schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle or user not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
            @Parameter(name = "username", description = "Username", in = QUERY, required = true),
            @Parameter(name = "requestedRole", description = "Role to set", in = QUERY, required = true),
    })
    public Mono<Role> changeUserRole(
            @PathVariable @NotBlank String shareCode,
            @RequestParam @NotBlank String username,
            @RequestParam @NotNull Role requestedRole,
            Authentication authentication
    ) {
        return userService.findUserByName(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User " + username + " not found"
                )))
                .map(User::getId)
                .flatMap(userId -> bundleService.changeUserRole(shareCode, userId, requestedRole, authentication));
    }

    @PostMapping("/{shareCode}/visibility")
    @Operation(
            summary = "Change bundle visibility",
            description = "Changes bundle visibility allowing it to be public or private"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed visibility",
                    content = @Content(schema = @Schema(implementation = BundleDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
            @Parameter(name = "isPublic", description = "Visibility flag", in = QUERY, required = true),
    })
    public Mono<BundleDto> changeVisibility(
            @PathVariable @NotBlank String shareCode,
            @RequestParam @NotNull Boolean isPublic,
            Authentication authentication
    ) {
        return bundleService.changeVisibility(shareCode, isPublic, authentication)
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }

    @PostMapping("/{shareCode}/problems")
    @Operation(
            summary = "Change bundle problems",
            description = "Changes problem list of a bundle"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed problem list",
                    content = @Content(schema = @Schema(implementation = BundleDto.class))),
            @ApiResponse(responseCode = "400", description = "Problem list should not be empty", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", in = PATH, required = true),
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ChangeBundleProblemsRequest.class)
            )
    )
    public Mono<BundleDto> changeProblems(
            @PathVariable @NotBlank String shareCode,
            @RequestBody @Valid ChangeBundleProblemsRequest changeBundleProblemsRequest,
            Authentication authentication
    ) {
        return bundleService.changeProblems(shareCode, changeBundleProblemsRequest.getProblemIds(), authentication)
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }
}
