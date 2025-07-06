package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata;
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest;
import io.github.sanyavertolet.edukate.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
@RequiredArgsConstructor
@Document(value = "bundles")
public class Bundle {
    @Id
    private String id;
    private String name;
    private String description;
    private Boolean isPublic;

    private List<String> problemIds;
    private Map<String, Role> userRoles;
    private Set<String> invitedUserIds;

    @Indexed(unique = true)
    private String shareCode = null;

    public Bundle updateShareCode(String shareCode) {
        this.shareCode = shareCode;
        return this;
    }

    public int addUser(String userId, Role role) {
        if (!userRoles.containsKey(userId)) {
            userRoles.put(userId, role);
            return 1;
        }
        return 0;
    }

    public int addUsers(Map<String, Role> usersWithRoles) {
        return usersWithRoles.entrySet()
                .stream()
                .map(entry ->addUser(entry.getKey(), entry.getValue()))
                .reduce(0, Integer::sum);
    }

    public int changeUserRole(String userId, Role newRole) {
        if (userRoles.containsKey(userId)) {
            userRoles.put(userId, newRole);
            return 1;
        }
        return 0;
    }

    public Role getUserRole(String userId) {
        return userRoles.getOrDefault(userId, null);
    }

    public boolean isUserInBundle(String userId) {
        return getUserRole(userId) != null;
    }

    public boolean isUserInvited(String userId) {
        return invitedUserIds != null && invitedUserIds.contains(userId);
    }

    public int inviteUser(String userId) {
        if (invitedUserIds == null) {
            invitedUserIds = new HashSet<>();
        }
        if (!invitedUserIds.contains(userId)) {
            invitedUserIds.add(userId);
            return 1;
        }
        return 0;
    }

    public int removeInvitedUser(String userId) {
        return invitedUserIds != null && invitedUserIds.remove(userId) ? 1 : 0;
    }

    public boolean isAdmin(String userId) {
        return getUserRole(userId).equals(Role.ADMIN);
    }

    public int removeUser(String userId) {
        return userRoles.remove(userId) != null ? 1 : 0;
    }

    public int removeUsers(List<String> userIds) {
        return userIds.stream().map(this::removeUser).reduce(0, Integer::sum);
    }

    public int addProblem(String problemId) {
        if (problemIds == null) {
            problemIds = new ArrayList<>();
        }
        if (!problemIds.contains(problemId)) {
            problemIds.add(problemId);
            return 1;
        }
        return 0;
    }

    public int removeProblem(String problemId) {
        return problemIds != null && problemIds.remove(problemId) ? 1 : 0;
    }

    public int addProblems(List<String> problemIds) {
        return problemIds.stream().map(this::addProblem).reduce(0, Integer::sum);
    }

    public int removeProblems(List<String> problemIds) {
        if (this.problemIds == null) {
            return 0;
        }
        return problemIds.stream().map(this::removeProblem).reduce(0, Integer::sum);
    }

    public List<String> getAdmins() {
        return userRoles.entrySet().stream().filter(entry -> entry.getValue().equals(Role.ADMIN)).map(Map.Entry::getKey).toList();
    }

    public static Bundle fromCreateRequest(CreateBundleRequest bundleRequest, String adminId) {
        return new Bundle(
                null,
                bundleRequest.getName(),
                bundleRequest.getDescription(),
                bundleRequest.getIsPublic(),
                bundleRequest.getProblemIds(),
                Map.of(adminId, Role.ADMIN),
                Set.of(),
                null
        );
    }

    public BundleDto toDto() {
        return new BundleDto(name, description, getAdmins(), isPublic, Collections.emptyList(), shareCode);
    }

    public BundleMetadata toBundleMetadata() {
        return new BundleMetadata(name, description, getAdmins(), shareCode, isPublic, (long) problemIds.size());
    }
}
