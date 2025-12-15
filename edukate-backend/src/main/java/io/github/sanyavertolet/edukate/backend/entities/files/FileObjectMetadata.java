package io.github.sanyavertolet.edukate.backend.entities.files;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileObjectMetadata {
    private Instant lastModified;
    private Long contentLength;
    private String contentType;
}
