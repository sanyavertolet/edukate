package io.github.sanyavertolet.edukate.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

import java.time.Instant;

@Data
@AllArgsConstructor
public class FileMetadata {
    @With
    private String key;
    private String authorName;
    private Instant lastModified;
    private Long size;

    public static FileMetadata of(String key, String authorName, Instant lastModified, Long size) {
        return new FileMetadata(key, authorName, lastModified, size);
    }
}
