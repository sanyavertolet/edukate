package io.github.sanyavertolet.edukate.backend.services.files;

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest;
import io.github.sanyavertolet.edukate.backend.entities.files.FileKey;
import io.github.sanyavertolet.edukate.backend.entities.files.SubmissionFileKey;
import io.github.sanyavertolet.edukate.backend.entities.files.TempFileKey;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class SubmissionFileService {
    private final BaseFileService baseFileService;

    public Mono<List<FileKey>> moveSubmissionFiles(String userId, String submissionId, CreateSubmissionRequest request) {
        return Flux.fromIterable(request.getFileNames())
                .flatMap(file -> baseFileService.moveFile(
                        TempFileKey.of(userId, file),
                        SubmissionFileKey.of(userId, request.getProblemId(), submissionId, file)))
                .collectList();
    }

    public Mono<Boolean> checkIfFilesExist(String userId, String problemId, String submissionId, List<String> fileNames) {
        return Flux.fromIterable(fileNames)
                .map(fileName -> SubmissionFileKey.of(userId, problemId, submissionId, fileName))
                .flatMap(baseFileService::doesFileExist)
                .all(Boolean::booleanValue);
    }
}
