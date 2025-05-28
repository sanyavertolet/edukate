package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto;
import io.github.sanyavertolet.edukate.notifier.dtos.SimpleNotificationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "notifications")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_type",
        defaultImpl = BaseNotification.class,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleNotification.class, name = "simple")
})
@JsonTypeName("base")
@TypeAlias("base")
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public sealed class BaseNotification permits SimpleNotification {
    @Id
    private ObjectId _id;
    @Indexed(unique = true)
    private String uuid;
    private Boolean isRead;
    private String userId;
    private LocalDateTime createdAt;

    /**
     * Convert a DTO to its corresponding entity.
     * This method handles polymorphic conversion from any DTO to the appropriate entity.
     *
     * @param dto The DTO to convert
     * @return The converted entity
     */
    public static BaseNotification fromDto(BaseNotificationDto dto) {
        if (dto instanceof SimpleNotificationDto simpleDto) {
            return SimpleNotification.fromDto(simpleDto);
        }
        throw new UnsupportedOperationException("Unsupported DTO type: " + dto.getClass().getName());
    }

    public BaseNotificationDto toDto() {
        if (this instanceof SimpleNotification simpleNotification) {
            return simpleNotification.toDto();
        }
        throw new UnsupportedOperationException("Unsupported Notification type: " + getClass().getName());
    }
}
