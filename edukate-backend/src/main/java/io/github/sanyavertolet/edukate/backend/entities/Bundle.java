package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata;
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest;
import io.github.sanyavertolet.edukate.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
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
    private Map<String, Role> userIdRoleMap;
    private Set<String> invitedUserIds;

    @With
    @Indexed(unique = true)
    private String shareCode = null;

    @SuppressWarnings("UnusedReturnValue")
    public boolean addUser(String userId, Role role) {
        return userIdRoleMap.putIfAbsent(userId, role) == null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean changeUserRole(String userId, Role newRole) {
        if (!userIdRoleMap.containsKey(userId)) {
            return false;
        }
        userIdRoleMap.put(userId, newRole);
        return true;
    }

    public Role getUserRole(String userId) {
        return userIdRoleMap.getOrDefault(userId, null);
    }

    public boolean isUserInBundle(String userId) {
        return getUserRole(userId) != null;
    }

    public boolean isUserInvited(String userId) {
        return invitedUserIds.contains(userId);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean inviteUser(String userId) {
        return invitedUserIds.add(userId);
    }

    public boolean removeInvitedUser(String userId) {
        return invitedUserIds.remove(userId);
    }

    public boolean isAdmin(String userId) {
        return Role.ADMIN.equals(getUserRole(userId));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeUser(String userId) {
        return userIdRoleMap.remove(userId) != null;
    }

    public List<String> getAdminIds() {
        return userIdRoleMap.entrySet().stream().filter(entry -> entry.getValue().equals(Role.ADMIN))
                .map(Map.Entry::getKey).toList();
    }

    public static Bundle fromCreateRequest(CreateBundleRequest bundleRequest, String adminId) {
        return new Bundle(
                null,
                bundleRequest.getName(),
                bundleRequest.getDescription(),
                Boolean.TRUE.equals(bundleRequest.getIsPublic()),
                new ArrayList<>(bundleRequest.getProblemIds() != null ? bundleRequest.getProblemIds() : List.of()),
                new HashMap<>(Map.of(adminId, Role.ADMIN)),
                new HashSet<>(),
                null
        );
    }

    public BundleDto toDto() {
        return new BundleDto(name, description, null, isPublic, null, shareCode);
    }

    public BundleMetadata toBundleMetadata() {
        return new BundleMetadata(name, description, null, shareCode, isPublic, (long) problemIds.size());
    }
}
