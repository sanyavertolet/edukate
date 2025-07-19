package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.*;
import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.backend.services.BundleService;
import io.github.sanyavertolet.edukate.backend.services.UserService;
import io.github.sanyavertolet.edukate.common.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bundles")
@Tag(name = "Bundles", description = "API for managing problem bundles and user collaborations")
public class BundleController {
    private final BundleService bundleService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Create a bundle",
            description = "Creates a new bundle with the specified properties"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created bundle",
                    content = @Content(schema = @Schema(implementation = BundleDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content)
    })
    @Parameters({
            @Parameter(name = "request", description = "Bundle creation details", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<BundleDto> createBundle(
            @RequestBody CreateBundleRequest request, 
            @Parameter(hidden = true) Authentication authentication
    ) {
        return bundleService.createBundle(request, authentication)
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }

    @GetMapping("/owned")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Get owned bundles",
            description = "Retrieves a paginated list of bundles owned by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved owned bundles",
                    content = @Content(schema = @Schema(implementation = BundleMetadata.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)"),
            @Parameter(name = "size", description = "Number of bundles per page"),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Flux<BundleMetadata> getOwnedBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return bundleService.getOwnedBundles(authentication, PageRequest.of(page, size)).map(Bundle::toBundleMetadata);
    }

    @GetMapping("/joined")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Get joined bundles",
            description = "Retrieves a paginated list of bundles joined by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved joined bundles",
                    content = @Content(schema = @Schema(implementation = BundleMetadata.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)"),
            @Parameter(name = "size", description = "Number of bundles per page"),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Flux<BundleMetadata> getJoinedBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return bundleService.getJoinedBundles(authentication, PageRequest.of(page, size)).map(Bundle::toBundleMetadata);
    }

    @GetMapping("/public")
    @Operation(
            summary = "Get public bundles",
            description = "Retrieves a paginated list of public bundles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved public bundles",
                    content = @Content(schema = @Schema(implementation = BundleMetadata.class)))
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)"),
            @Parameter(name = "size", description = "Number of bundles per page")
    })
    public Flux<BundleMetadata> getPublicBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bundleService.getPublicBundles(PageRequest.of(page, size)).map(Bundle::toBundleMetadata);
    }

    @GetMapping("/{shareCode}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Get bundle by share code",
            description = "Retrieves a bundle by its share code (user must be a member of the bundle)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bundle",
                    content = @Content(schema = @Schema(implementation = BundleDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of the bundle", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<BundleDto> getBundleByShareCode(
            @PathVariable String shareCode,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return bundleService.findBundleByShareCode(shareCode)
                .filter(bundle -> bundle.isUserInBundle(authentication.getName()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You must be a member of the bundle to view it."
                )))
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }

    @PostMapping("/{shareCode}/join")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Join a bundle",
            description = "Joins a bundle using its share code"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined bundle",
                    content = @Content(schema = @Schema(implementation = BundleMetadata.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<BundleMetadata> joinBundle(
            @PathVariable String shareCode, 
            @Parameter(hidden = true) Authentication authentication
    ) {
        return bundleService.joinUser(authentication.getName(), shareCode).map(Bundle::toBundleMetadata);
    }

    @PostMapping("/{shareCode}/leave")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Leave a bundle",
            description = "Leaves a bundle the user is currently a member of"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully left bundle",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found or user not a member", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<String> leaveBundle(
            @PathVariable String shareCode, 
            @Parameter(hidden = true) Authentication authentication
    ) {
        return bundleService.removeUser(authentication.getName(), shareCode).map(Bundle::getShareCode);
    }

    @PostMapping("/{shareCode}/invite")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Invite user to bundle",
            description = "Invites another user to join a bundle"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully invited user",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role or insufficient permissions in bundle", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle or user not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", required = true),
            @Parameter(name = "inviteeName", description = "Username of the user to invite", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<String> inviteToBundle(
            @PathVariable String shareCode, 
            @RequestParam String inviteeName, 
            @Parameter(hidden = true) Authentication authentication
    ) {
        return Mono.just(inviteeName)
                .filterWhen(userService::doesUserExist)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User " + inviteeName +  " not found"
                )))
                .flatMap(invitee -> bundleService.inviteUser(authentication.getName(), invitee, shareCode))
                .thenReturn("User " + inviteeName + " has been invited to bundle " + shareCode);
    }

    @PostMapping("/{shareCode}/reply-invite")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Reply to bundle invitation",
            description = "Accepts or declines an invitation to join a bundle"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully replied to invitation",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle or invitation not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", required = true),
            @Parameter(name = "response", description = "True to accept, false to decline", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<String> replyToInvite(
            @PathVariable String shareCode, 
            @RequestParam Boolean response, 
            @Parameter(hidden = true) Authentication authentication
    ) {
        return Mono.just(response)
                .flatMap(hasAccepted -> {
                    if (hasAccepted) {
                        return bundleService.joinUser(authentication.getName(), shareCode)
                                .thenReturn("You have accepted invite to bundle " + shareCode);
                    }
                    return bundleService.declineInvite(shareCode, authentication)
                            .thenReturn("You have declined invite to bundle " + shareCode);
                });
    }

    @GetMapping("/{shareCode}/users")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Get users in bundle",
            description = "Retrieves a list of users in a bundle with their roles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                    content = @Content(schema = @Schema(implementation = UserNameWithRole.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role or user not in bundle", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bundle not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "shareCode", description = "Bundle share code", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<List<UserNameWithRole>> getUsersInBundle(
            @PathVariable String shareCode, 
            @Parameter(hidden = true) Authentication authentication
    ) {
        return bundleService.getBundleUsers(shareCode, authentication).flatMap(bundleService::mapToList);
    }

    @PostMapping("/{shareCode}/role")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<Role> changeUserRole(
            @PathVariable String shareCode,
            @RequestParam String username,
            @RequestParam Role requestedRole,
            Authentication authentication
    ) {
        return bundleService.changeUserRole(shareCode, username, requestedRole, authentication);
    }

    @PostMapping("/{shareCode}/visibility")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleDto> changeVisibility(
            @PathVariable String shareCode,
            @RequestParam Boolean isPublic,
            Authentication authentication
    ) {
        return bundleService.changeVisibility(shareCode, isPublic, authentication)
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }

    @PostMapping("/{shareCode}/problems")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleDto> changeProblems(
            @PathVariable String shareCode,
            @RequestBody ChangeBundleProblemsRequest changeBundleProblemsRequest,
            Authentication authentication
    ) {
        return bundleService.changeProblems(shareCode, changeBundleProblemsRequest.problemIds(), authentication)
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }
}
