package io.github.sanyavertolet.edukate.storage

import io.github.sanyavertolet.edukate.storage.configs.S3Properties
import java.nio.ByteBuffer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner

abstract class AbstractStorage<Key : Any, Metadata : Any>(
    s3AsyncClient: S3AsyncClient,
    s3Presigner: S3Presigner,
    s3Properties: S3Properties,
) : AbstractReadOnlyStorage<Key, Metadata>(s3AsyncClient, s3Presigner, s3Properties), Storage<Key, Metadata> {
    override fun delete(key: Key): Mono<Boolean> {
        val request = DeleteObjectRequest.builder().bucket(s3Properties.bucket).key(key.toString()).build()

        return Mono.fromFuture(s3AsyncClient.deleteObject(request))
            .map { response -> response.sdkHttpResponse().isSuccessful }
            // fixme: need something better than this
            .onErrorResume { Mono.just(false) }
    }

    override fun deleteAll(keys: Collection<Key>): Mono<Boolean> {
        val delete = Delete.builder().objects(keys.map { ObjectIdentifier.builder().key(it.toString()).build() }).build()

        val request = DeleteObjectsRequest.builder().bucket(s3Properties.bucket).delete(delete).build()

        return Mono.fromFuture(s3AsyncClient.deleteObjects(request)).map { response -> !response.hasErrors() }
    }

    override fun upload(key: Key, contentLength: Long, contentType: String, content: Flux<ByteBuffer>): Mono<Key> {
        val request =
            PutObjectRequest.builder()
                .bucket(s3Properties.bucket)
                .key(key.toString())
                .contentLength(contentLength)
                .contentType(contentType)
                .build()

        return Mono.fromFuture { s3AsyncClient.putObject(request, AsyncRequestBody.fromPublisher(content)) }.thenReturn(key)
    }

    override fun move(source: Key, target: Key): Mono<Boolean> {
        val copyRequest =
            CopyObjectRequest.builder()
                .sourceBucket(s3Properties.bucket)
                .sourceKey(source.toString())
                .destinationBucket(s3Properties.bucket)
                .destinationKey(target.toString())
                .build()

        return Mono.fromFuture(s3AsyncClient.copyObject(copyRequest)).flatMap { delete(source) }
    }
}
