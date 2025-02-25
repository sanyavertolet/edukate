package io.github.sanyavertolet.edukate.backend.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class S3Storage implements Storage<String> {
    private final S3AsyncClient s3AsyncClient;
    private final String bucket;

    @Override
    public Flux<String> list() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();

        return Flux.from(s3AsyncClient.listObjectsV2Paginator(request))
                .flatMap(response -> Flux.fromIterable(response.contents()))
                .map(S3Object::key);
    }

    @Override
    public Mono<Boolean> doesExist(String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(_ -> true)
                .onErrorResume(NoSuchKeyException.class, _ -> Mono.just(false));
    }

    @Override
    public Mono<Long> contentLength(String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(HeadObjectResponse::contentLength);
    }

    @Override
    public Mono<Instant> lastModified(String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(HeadObjectResponse::lastModified);
    }

    @Override
    public Mono<Boolean> delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.deleteObject(request))
                .map(_ -> true)
                .onErrorResume(_ -> Mono.just(false));
    }

    @Override
    public Mono<Boolean> deleteAll(Collection<String> keys) {
        Delete delete = Delete.builder()
                .objects(keys.stream()
                        .map(key -> ObjectIdentifier.builder().key(key).build())
                        .toList())
                .build();

        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(delete)
                .build();

        return Mono.fromFuture(s3AsyncClient.deleteObjects(request))
                .map(response -> !response.hasErrors());
    }

    @Override
    public Mono<String> upload(String key, Flux<ByteBuffer> content) {
        return content.collectList()
                .flatMap(buffers -> {
                    int totalSize = buffers.stream()
                            .mapToInt(ByteBuffer::remaining)
                            .sum();
                    return upload(key, totalSize, Flux.fromIterable(buffers));
                });
    }

    @Override
    public Mono<String> upload(String key, long contentLength, Flux<ByteBuffer> content) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength(contentLength)
                .build();

        return Mono.fromFuture(() -> s3AsyncClient.putObject(
                request,
                AsyncRequestBody.fromPublisher(content)
        )).map(_ -> key);
    }

    @Override
    public Flux<ByteBuffer> download(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return Mono.fromFuture(() -> s3AsyncClient.getObject(
                request,
                AsyncResponseTransformer.toPublisher()
        )).flatMapMany(Flux::from);
    }

    @Override
    public Mono<Boolean> move(String source, String target) {
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(source)
                .destinationBucket(bucket)
                .destinationKey(target)
                .build();

        return Mono.fromFuture(s3AsyncClient.copyObject(copyRequest))
                .flatMap(_ -> delete(source));
    }

    @Override
    public Mono<String> getDownloadUrl(String key) {
        return Mono.fromCallable(() -> {
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return s3AsyncClient.utilities().getUrl(getUrlRequest).toString();
        });
    }
}
