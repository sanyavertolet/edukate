package io.github.sanyavertolet.edukate.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.Collection;

public abstract class AbstractStorage<Key> extends AbstractReadOnlyStorage<Key> implements Storage<Key> {

    public AbstractStorage(S3AsyncClient s3AsyncClient, String bucket) {
        super(s3AsyncClient, bucket);
    }

    protected abstract Key buildKey(String stringKey);

    @Override
    public Mono<Boolean> delete(Key key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.deleteObject(request))
                .map(_ -> true)
                .onErrorResume(_ -> Mono.just(false));
    }

    @Override
    public Mono<Boolean> deleteAll(Collection<Key> keys) {
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
    public Mono<Key> upload(Key key, Flux<ByteBuffer> content) {
        return content.collectList()
                .flatMap(buffers -> {
                    int totalSize = buffers.stream()
                            .mapToInt(ByteBuffer::remaining)
                            .sum();
                    return upload(key, totalSize, Flux.fromIterable(buffers));
                });
    }

    @Override
    public Mono<Key> upload(Key key, long contentLength, Flux<ByteBuffer> content) {
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
    public Mono<Boolean> move(Key source, Key target) {
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(source.toString())
                .destinationBucket(bucket)
                .destinationKey(target.toString())
                .build();

        return Mono.fromFuture(s3AsyncClient.copyObject(copyRequest))
                .flatMap(_ -> delete(source));
    }
}
