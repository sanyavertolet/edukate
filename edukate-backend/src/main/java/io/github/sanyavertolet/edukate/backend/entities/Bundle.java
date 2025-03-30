package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
@RequiredArgsConstructor
@Document(value = "bundles")
@CompoundIndex(name = "bundle_owner_id_name_idx", def = "{'ownerId': 1, 'name': 1}", unique = true)
public class Bundle {
    @Id
    private String id;
    private String name;
    private String description;
    private String ownerId;
    private Boolean isPublic;

    private List<String> problemIds;
    private List<String> userIds;

    @Indexed(unique = true)
    private String shareCode = null;

    public Bundle updateShareCode(String shareCode) {
        this.shareCode = shareCode;
        return this;
    }

    public int addUser(String userId) {
        if (userIds == null) {
            userIds = new ArrayList<>();
        }
        if (!userIds.contains(userId)) {
            userIds.add(userId);
            return 1;
        }
        return 0;
    }

    public int addUsers(List<String> userIds) {
        return userIds.stream().map(this::addUser).reduce(0, Integer::sum);
    }

    public boolean isUserInBundle(String userId) {
        return userIds != null && userIds.contains(userId);
    }

    public boolean isOwner(String userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    public int removeUser(String userId) {
        return userIds != null && userIds.remove(userId) ? 1 : 0;
    }

    public int removeUsers(List<String> userIds) {
        if (this.userIds == null) {
            return 0;
        }
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

    public static Bundle fromDto(BundleDto dto) {
        return new Bundle(
                null,
                dto.getName(),
                dto.getDescription(),
                dto.getOwnerName(),
                dto.getIsPublic(),
                dto.getProblemIds(),
                new ArrayList<>(),
                null
        );
    }

    public BundleDto toDto() {
        return new BundleDto(name, description, ownerId, isPublic, problemIds, shareCode);
    }

    public BundleMetadata toBundleMetadata() {
        return new BundleMetadata(name, description, ownerId, shareCode, isPublic, (long) problemIds.size());
    }
}
