package io.github.sanyavertolet.edukate.storage

import io.github.sanyavertolet.edukate.storage.configs.S3Properties
import java.nio.ByteBuffer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest

private const val HTTP_NOT_FOUND = 404

abstract class AbstractReadOnlyStorage<Key : Any, Metadata : Any>(
    protected val s3AsyncClient: S3AsyncClient,
    protected val s3Presigner: S3Presigner,
    protected val s3Properties: S3Properties,
) : ReadOnlyStorage<Key, Metadata> {
    protected abstract fun buildKey(key: String): Key

    protected abstract fun buildMetadata(headObjectResponse: HeadObjectResponse): Metadata

    override fun metadata(key: Key): Mono<Metadata> {
        val request = HeadObjectRequest.builder().bucket(s3Properties.bucket).key(key.toString()).build()

        return Mono.fromFuture(s3AsyncClient.headObject(request))
            .onErrorResume(S3Exception::class.java) { e ->
                if (e.statusCode() == HTTP_NOT_FOUND) Mono.empty() else Mono.error(e)
            }
            .map { buildMetadata(it) }
    }

    override fun getContent(key: Key): Flux<ByteBuffer> {
        val request = GetObjectRequest.builder().bucket(s3Properties.bucket).key(key.toString()).build()

        return Mono.fromFuture { s3AsyncClient.getObject(request, AsyncResponseTransformer.toPublisher()) }
            .onErrorResume(S3Exception::class.java) { e ->
                if (e.statusCode() == HTTP_NOT_FOUND) Mono.empty() else Mono.error(e)
            }
            .flatMapMany { Flux.from(it) }
    }

    override fun generatePresignedUrl(key: Key): Mono<String> =
        Mono.fromCallable {
            val get = GetObjectRequest.builder().bucket(s3Properties.bucket).key(key.toString()).build()
            val req =
                GetObjectPresignRequest.builder()
                    .signatureDuration(s3Properties.signatureDuration)
                    .getObjectRequest(get)
                    .build()
            s3Presigner.presignGetObject(req).url().toString()
        }

    override fun prefixed(rawKeyPrefix: String): Flux<Key> {
        val request = ListObjectsV2Request.builder().bucket(s3Properties.bucket).prefix(rawKeyPrefix).build()

        return Flux.from(s3AsyncClient.listObjectsV2Paginator(request))
            .flatMap { response -> Flux.fromIterable(response.contents()) }
            .map(S3Object::key)
            .map { buildKey(it) }
    }
}
