package io.github.sanyavertolet.edukate.backend.storage;

import io.github.sanyavertolet.edukate.backend.entities.files.FileKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Storage implements Storage<FileKey> {
    private final S3AsyncClient s3AsyncClient;
    private final String bucket;

    @Override
    public Flux<FileKey> prefixedList(String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        return Flux.from(s3AsyncClient.listObjectsV2Paginator(request))
                .flatMap(response -> Flux.fromIterable(response.contents()))
                .map(S3Object::key)
                .flatMap(key -> {
                    try {
                        return Mono.just(FileKey.of(key));
                    } catch (IllegalArgumentException ex) {
                        log.warn("Invalid file key: {}, skipping", key);
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Boolean> doesExist(FileKey key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(_ -> true)
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.just(false) : Mono.error(e));
    }

    @Override
    public Mono<Long> contentLength(FileKey key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(HeadObjectResponse::contentLength)
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.empty() : Mono.error(e));
    }

    @Override
    public Mono<Instant> lastModified(FileKey key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(request))
                .map(HeadObjectResponse::lastModified)
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.empty() : Mono.error(e));
    }

    @Override
    public Mono<Boolean> delete(FileKey key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.deleteObject(request))
                .map(_ -> true)
                .onErrorResume(_ -> Mono.just(false));
    }

    @Override
    public Mono<Boolean> deleteAll(Collection<FileKey> keys) {
        Delete delete = Delete.builder()
                .objects(keys.stream().map(key -> ObjectIdentifier.builder().key(key.toString()).build()).toList())
                .build();

        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(delete)
                .build();

        return Mono.fromFuture(s3AsyncClient.deleteObjects(request))
                .map(response -> !response.hasErrors());
    }

    @Override
    public Mono<FileKey> upload(FileKey key, Flux<ByteBuffer> content) {
        return content.collectList()
                .flatMap(buffers -> {
                    int totalSize = buffers.stream()
                            .mapToInt(ByteBuffer::remaining)
                            .sum();
                    return upload(key, totalSize, Flux.fromIterable(buffers));
                });
    }

    @Override
    public Mono<FileKey> upload(FileKey key, long contentLength, Flux<ByteBuffer> content) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .contentLength(contentLength)
                .build();

        return Mono.fromFuture(() -> s3AsyncClient.putObject(
                request,
                AsyncRequestBody.fromPublisher(content)
        ))
                .map(_ -> key);
    }

    @Override
    public Flux<ByteBuffer> download(FileKey key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(() -> s3AsyncClient.getObject(request, AsyncResponseTransformer.toPublisher()))
                .flatMapMany(Flux::from)
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Flux.empty() : Flux.error(e));
    }

    @Override
    public Mono<Boolean> move(FileKey source, FileKey target) {
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(source.toString())
                .destinationBucket(bucket)
                .destinationKey(target.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.copyObject(copyRequest))
                .flatMap(_ -> delete(source));
    }

    @Override
    public Mono<String> getDownloadUrl(FileKey key) {
        return Mono.fromCallable(() -> {
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(key.toString())
                    .build();
            return s3AsyncClient.utilities().getUrl(getUrlRequest).toString();
        });
    }
}
