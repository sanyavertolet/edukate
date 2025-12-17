package io.github.sanyavertolet.edukate.checker.storage;

import io.github.sanyavertolet.edukate.storage.AbstractReadOnlyStorage;
import io.github.sanyavertolet.edukate.storage.configs.S3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Component
public class RawKeyReadOnlyStorage extends AbstractReadOnlyStorage<String, MediaType> {
    private static final Logger log = LoggerFactory.getLogger(RawKeyReadOnlyStorage.class);

    public RawKeyReadOnlyStorage(S3AsyncClient s3AsyncClient, S3Presigner s3Presigner, S3Properties s3Properties) {
        super(s3AsyncClient, s3Presigner, s3Properties);
    }

    @Override
    protected String buildKey(String stringKey) {
        return stringKey;
    }

    @Override
    protected MediaType buildMetadata(HeadObjectResponse headObjectResponse) {
        log.debug("Identified content type of: {}", headObjectResponse.contentType());
        return MediaType.parseMediaType(headObjectResponse.contentType());
    }
}
