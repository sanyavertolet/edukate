package io.github.sanyavertolet.edukate.storage;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.time.Instant;

@Slf4j
abstract public class AbstractReadOnlyStorage<Key> implements ReadOnlyStorage<Key> {
    protected final S3AsyncClient s3AsyncClient;
    protected final String bucket;

    public AbstractReadOnlyStorage(S3AsyncClient s3AsyncClient, String bucket) {
        this.s3AsyncClient = s3AsyncClient;
        this.bucket = bucket;
    }

    protected abstract Key buildKey(String stringKey);

    @Override
    public Flux<Key> prefixedList(String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        return Flux.from(s3AsyncClient.listObjectsV2Paginator(request))
                .flatMap(response -> Flux.fromIterable(response.contents()))
                .map(S3Object::key)
                .flatMap(key -> {
                    try {
                        return Mono.just(buildKey(key));
                    } catch (IllegalArgumentException ex) {
                        log.warn("Invalid file key: {}, skipping", key);
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Boolean> doesExist(Key key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(_ -> true)
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.just(false) : Mono.error(e));
    }

    @Override
    public Mono<Long> contentLength(Key key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(HeadObjectResponse::contentLength)
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.empty() : Mono.error(e));
    }

    @Override
    public Mono<Instant> lastModified(Key key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(HeadObjectResponse::lastModified)
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.empty() : Mono.error(e));
    }



    @Override
    public Flux<ByteBuffer> download(Key key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(() -> s3AsyncClient.getObject(request, AsyncResponseTransformer.toPublisher()))
                .flatMapMany(Flux::from)
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Flux.empty() : Flux.error(e));
    }

    @Override
    public Mono<String> getDownloadUrl(Key key) {
        return Mono.fromCallable(() -> {
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(key.toString())
                    .build();
            return s3AsyncClient.utilities().getUrl(getUrlRequest).toString();
        });
    }
}
