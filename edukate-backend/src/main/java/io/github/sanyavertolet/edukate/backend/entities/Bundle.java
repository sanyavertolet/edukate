package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata;
import io.github.sanyavertolet.edukate.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return usersWithRoles.entrySet().stream().map(entry -> addUser(entry.getKey(), entry.getValue())).reduce(0, Integer::sum);
    }

    public int changeUserRole(String userId, Role newRole) {
        if (userRoles.containsKey(userId)) {
            userRoles.put(userId, newRole);
            return 1;
        }
        return 0;
    }

    public boolean isUserInBundle(String userId) {
        return userRoles.containsKey(userId);
    }

    public boolean isAdmin(String userId) {
        return userRoles.getOrDefault(userId, null).equals(Role.ADMIN);
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

    public static Bundle fromDto(BundleDto dto) {
        Map<String, Role> users = dto.getAdmins().stream().collect(
                HashMap::new,
                (map, admin) -> map.put(admin, Role.ADMIN),
                HashMap::putAll
        );
        return new Bundle(
                null,
                dto.getName(),
                dto.getDescription(),
                dto.getIsPublic(),
                dto.getProblemIds(),
                users,
                null
        );
    }

    public BundleDto toDto() {
        return new BundleDto(name, description, getAdmins(), isPublic, problemIds, shareCode);
    }

    public BundleMetadata toBundleMetadata() {
        return new BundleMetadata(name, description, getAdmins(), shareCode, isPublic, (long) problemIds.size());
    }
}
