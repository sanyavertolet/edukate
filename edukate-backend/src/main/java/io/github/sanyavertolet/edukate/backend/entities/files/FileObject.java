package io.github.sanyavertolet.edukate.backend.entities.files;

import io.github.sanyavertolet.edukate.storage.keys.FileKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file_objects")
public class FileObject {
    @Id
    private String id;

    @Indexed(unique = true)
    private String keyPath;

    private FileKey key;

    private String type;

    private String ownerUserId;

    private FileObjectMetadata metadata;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder.Default
    private Integer metaVersion = 1;
}
