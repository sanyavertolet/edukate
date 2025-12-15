package io.github.sanyavertolet.edukate.storage;

import io.github.sanyavertolet.edukate.storage.configs.S3Properties;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.ByteBuffer;

@Slf4j
abstract public class AbstractReadOnlyStorage<Key, Metadata> implements ReadOnlyStorage<Key, Metadata> {
    protected final S3AsyncClient s3AsyncClient;
    protected final S3Presigner s3Presigner;
    protected final S3Properties s3Properties;

    public AbstractReadOnlyStorage(S3AsyncClient s3AsyncClient, S3Presigner s3Presigner, S3Properties s3Properties) {
        this.s3AsyncClient = s3AsyncClient;
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
    }

    protected abstract Key buildKey(String key);

    protected abstract Metadata buildMetadata(HeadObjectResponse headObjectResponse);

    @Override
    public Mono<Metadata> metadata(Key key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.empty() : Mono.error(e))
                .map(this::buildMetadata);
    }

    @Override
    public Flux<ByteBuffer> getContent(Key key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key.toString())
                .build();

        return Mono.fromFuture(() -> s3AsyncClient.getObject(request, AsyncResponseTransformer.toPublisher()))
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.empty() : Mono.error(e))
                .flatMapMany(Flux::from);
    }

    @Override
    public Mono<String> generatePresignedUrl(Key key) {
        return Mono.fromCallable(() -> {
            GetObjectRequest get = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key.toString())
                    .build();
            GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                    .signatureDuration(s3Properties.getSignatureDuration())
                    .getObjectRequest(get)
                    .build();
            return s3Presigner.presignGetObject(req).url().toString();
        });
    }

    @Override
    public Flux<Key> prefixed(String rawKeyPrefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Properties.getBucket())
                .prefix(rawKeyPrefix)
                .build();

        return Flux.from(s3AsyncClient.listObjectsV2Paginator(request))
                .flatMap(response -> Flux.fromIterable(response.contents()))
                .map(S3Object::key)
                .map(this::buildKey);
    }
}
