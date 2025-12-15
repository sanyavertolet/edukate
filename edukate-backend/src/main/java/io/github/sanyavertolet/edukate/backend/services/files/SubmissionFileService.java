package io.github.sanyavertolet.edukate.backend.services.files;

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest;
import io.github.sanyavertolet.edukate.storage.keys.FileKey;
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey;
import io.github.sanyavertolet.edukate.storage.keys.TempFileKey;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class SubmissionFileService {
    private final FileManager fileManager;

    public Flux<FileKey> moveSubmissionFiles(String userId, String submissionId, CreateSubmissionRequest request) {
        return Flux.fromIterable(request.getFileNames())
                .flatMapSequential(file -> fileManager.moveFile(
                        TempFileKey.of(userId, file),
                        SubmissionFileKey.of(userId, request.getProblemId(), submissionId, file))
                );
    }
}
