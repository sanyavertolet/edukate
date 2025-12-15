package io.github.sanyavertolet.edukate.backend.storage;

import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata;
import io.github.sanyavertolet.edukate.storage.AbstractStorage;
import io.github.sanyavertolet.edukate.storage.configs.S3Properties;
import io.github.sanyavertolet.edukate.storage.keys.FileKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Component
public class FileKeyStorage extends AbstractStorage<FileKey, FileObjectMetadata> {

    public FileKeyStorage(S3AsyncClient s3AsyncClient, S3Presigner s3Presigner, S3Properties s3Properties) {
        super(s3AsyncClient, s3Presigner, s3Properties);
    }

    @Override
    protected FileKey buildKey(String key) {
        return FileKey.of(key);
    }

    @Override
    protected FileObjectMetadata buildMetadata(HeadObjectResponse headObjectResponse) {
        return FileObjectMetadata.builder()
                .lastModified(headObjectResponse.lastModified())
                .contentLength(headObjectResponse.contentLength())
                .contentType(headObjectResponse.contentType())
                .build();
    }
}
