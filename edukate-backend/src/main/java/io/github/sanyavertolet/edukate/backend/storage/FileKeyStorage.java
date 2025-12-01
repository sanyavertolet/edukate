package io.github.sanyavertolet.edukate.backend.storage;

import io.github.sanyavertolet.edukate.storage.AbstractStorage;
import io.github.sanyavertolet.edukate.storage.configs.S3Properties;
import io.github.sanyavertolet.edukate.storage.keys.FileKey;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Component
public class FileKeyStorage extends AbstractStorage<FileKey> {

    public FileKeyStorage(S3AsyncClient s3AsyncClient, S3Presigner s3Presigner, S3Properties s3Properties) {
        super(s3AsyncClient, s3Presigner, s3Properties);
    }

    @Override
    protected FileKey buildKey(String rawKey) {
        return FileKey.of(rawKey);
    }
}
