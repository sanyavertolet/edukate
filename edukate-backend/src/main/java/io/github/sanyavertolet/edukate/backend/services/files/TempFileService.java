package io.github.sanyavertolet.edukate.backend.services.files;

import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata;
import io.github.sanyavertolet.edukate.backend.entities.files.FileKey;
import io.github.sanyavertolet.edukate.backend.entities.files.TempFileKey;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Service
@AllArgsConstructor
public class TempFileService {
    private final BaseFileService baseFileService;

    public Mono<Boolean> doesExist(String userId, String fileName) {
        return baseFileService.doesFileExist(TempFileKey.of(userId, fileName));
    }

    public Flux<FileKey> listFiles(String userId) {
        return baseFileService.listFilesWithPrefix(TempFileKey.prefix(userId));
    }

    public Flux<FileMetadata> listFileMetadata(String userId) {
        return baseFileService.listFileMetadataWithPrefix(TempFileKey.prefix(userId), userId);
    }

    public Mono<FileKey> uploadFile(String userId, String fileName, Flux<ByteBuffer> content) {
        return baseFileService.uploadFile(TempFileKey.of(userId, fileName), content);
    }

    public Mono<Boolean> deleteFile(String userId, String fileName) {
        return baseFileService.deleteFile(TempFileKey.of(userId, fileName));
    }

    public Flux<ByteBuffer> getFile(String userId, String fileName) {
        return baseFileService.getFile(TempFileKey.of(userId, fileName));
    }
}
