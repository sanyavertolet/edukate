@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.storage

import io.github.sanyavertolet.edukate.storage.configs.S3Properties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.nio.ByteBuffer
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.reactivestreams.Subscriber
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.core.async.ResponsePublisher
import software.amazon.awssdk.http.SdkHttpResponse
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.CopyObjectResponse
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Publisher
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest

class AbstractStorageTest {
    private val s3Client: S3AsyncClient = mockk()
    private val s3Presigner: S3Presigner = mockk()
    private val props =
        S3Properties(
            endpoint = "http://localhost:9000",
            region = "us-east-1",
            accessKey = "test",
            secretKey = "test",
            bucket = "test-bucket",
            signatureDuration = Duration.ofHours(1),
        )

    private lateinit var storage: TestStorage

    @BeforeEach
    fun setUp() {
        storage = TestStorage(s3Client, s3Presigner, props)
    }

    // region test helpers

    /** Concrete subclass that maps keys to strings and metadata to content type. */
    class TestStorage(s3Client: S3AsyncClient, presigner: S3Presigner, props: S3Properties) :
        AbstractStorage<String, String>(s3Client, presigner, props) {

        override fun buildKey(key: String): String = key

        override fun buildMetadata(headObjectResponse: HeadObjectResponse): String = headObjectResponse.contentType()
    }

    private fun responsePublisher(
        response: GetObjectResponse,
        content: List<ByteBuffer>,
    ): ResponsePublisher<GetObjectResponse> {
        val publisher = mockk<ResponsePublisher<GetObjectResponse>>()
        every { publisher.response() } returns response
        every { publisher.subscribe(any<Subscriber<in ByteBuffer>>()) } answers
            {
                Flux.fromIterable(content).subscribe(firstArg<Subscriber<in ByteBuffer>>())
            }
        return publisher
    }

    private fun headResponse(contentType: String = "image/png") =
        HeadObjectResponse.builder()
            .contentType(contentType)
            .contentLength(42L)
            .lastModified(Instant.parse("2026-01-01T00:00:00Z"))
            .build()

    private fun getResponse(contentType: String = "image/png") =
        GetObjectResponse.builder()
            .contentType(contentType)
            .contentLength(42L)
            .lastModified(Instant.parse("2026-01-01T00:00:00Z"))
            .build()

    private fun s3Error(statusCode: Int, message: String = "error"): S3Exception =
        S3Exception.builder().statusCode(statusCode).message(message).build() as S3Exception

    private fun successfulHttpResponse(): SdkHttpResponse = SdkHttpResponse.builder().statusCode(200).build()

    private fun mockGetObject(publisher: ResponsePublisher<GetObjectResponse>) {
        every {
            s3Client.getObject(any<GetObjectRequest>(), any<AsyncResponseTransformer<GetObjectResponse, Any>>())
        } returns CompletableFuture.completedFuture(publisher)
    }

    private fun mockGetObjectError(error: Throwable) {
        every {
            s3Client.getObject(any<GetObjectRequest>(), any<AsyncResponseTransformer<GetObjectResponse, Any>>())
        } returns CompletableFuture.failedFuture(error)
    }

    private fun listPublisher(vararg keys: String): ListObjectsV2Publisher {
        val pub = mockk<ListObjectsV2Publisher>()
        every { pub.subscribe(any<Subscriber<in ListObjectsV2Response>>()) } answers
            {
                val response =
                    ListObjectsV2Response.builder().contents(keys.map { S3Object.builder().key(it).build() }).build()
                Flux.just(response).subscribe(firstArg<Subscriber<in ListObjectsV2Response>>())
            }
        return pub
    }

    private fun mockDeleteSuccess(): DeleteObjectResponse {
        val response = mockk<DeleteObjectResponse>()
        every { response.sdkHttpResponse() } returns successfulHttpResponse()
        every { s3Client.deleteObject(any<DeleteObjectRequest>()) } returns CompletableFuture.completedFuture(response)
        return response
    }

    // endregion

    // region metadata()

    @Test
    fun `metadata returns mapped value on success`() {
        every { s3Client.headObject(any<HeadObjectRequest>()) } returns
            CompletableFuture.completedFuture(headResponse("application/pdf"))

        StepVerifier.create(storage.metadata("some-key"))
            .assertNext { assertThat(it).isEqualTo("application/pdf") }
            .verifyComplete()
    }

    @Test
    fun `metadata returns empty on 404`() {
        every { s3Client.headObject(any<HeadObjectRequest>()) } returns CompletableFuture.failedFuture(s3Error(404))

        StepVerifier.create(storage.metadata("missing-key")).verifyComplete()
    }

    @Test
    fun `metadata propagates non-404 S3 errors`() {
        every { s3Client.headObject(any<HeadObjectRequest>()) } returns
            CompletableFuture.failedFuture(s3Error(500, "Internal Server Error"))

        StepVerifier.create(storage.metadata("some-key")).expectError(S3Exception::class.java).verify()
    }

    @Test
    fun `metadata sends correct bucket and key in request`() {
        every { s3Client.headObject(any<HeadObjectRequest>()) } returns CompletableFuture.completedFuture(headResponse())

        StepVerifier.create(storage.metadata("books/savchenko/problems/1.1.1/img.png"))
            .expectNextCount(1)
            .verifyComplete()

        verify {
            s3Client.headObject(
                match<HeadObjectRequest> {
                    it.bucket() == "test-bucket" && it.key() == "books/savchenko/problems/1.1.1/img.png"
                }
            )
        }
    }

    // endregion

    // region getContent()

    @Test
    fun `getContent returns byte stream on success`() {
        val bytes = "hello".toByteArray()
        mockGetObject(responsePublisher(getResponse(), listOf(ByteBuffer.wrap(bytes))))

        StepVerifier.create(storage.getContent("some-key"))
            .assertNext { buf ->
                val result = ByteArray(buf.remaining())
                buf.get(result)
                assertThat(result).isEqualTo(bytes)
            }
            .verifyComplete()
    }

    @Test
    fun `getContent streams multiple chunks in order`() {
        val chunk1 = "first-".toByteArray()
        val chunk2 = "second".toByteArray()
        mockGetObject(responsePublisher(getResponse(), listOf(ByteBuffer.wrap(chunk1), ByteBuffer.wrap(chunk2))))

        StepVerifier.create(storage.getContent("multi-chunk").map { buf -> ByteArray(buf.remaining()).also { buf.get(it) } })
            .assertNext { assertThat(it).isEqualTo(chunk1) }
            .assertNext { assertThat(it).isEqualTo(chunk2) }
            .verifyComplete()
    }

    @Test
    fun `getContent returns empty flux on 404`() {
        mockGetObjectError(s3Error(404))

        StepVerifier.create(storage.getContent("missing-key")).verifyComplete()
    }

    @Test
    fun `getContent propagates non-404 S3 errors`() {
        mockGetObjectError(s3Error(500))

        StepVerifier.create(storage.getContent("some-key")).expectError(S3Exception::class.java).verify()
    }

    // endregion

    // region getContentWithMetadata()

    @Test
    fun `getContentWithMetadata returns content and metadata from single call`() {
        val bytes = "image-data".toByteArray()
        mockGetObject(responsePublisher(getResponse("image/jpeg"), listOf(ByteBuffer.wrap(bytes))))

        StepVerifier.create(storage.getContentWithMetadata("some-key"))
            .assertNext { cwm ->
                assertThat(cwm.metadata).isEqualTo("image/jpeg")
                val result =
                    cwm.content.collectList().block()!!.let { bufs ->
                        ByteArray(bufs.sumOf { it.remaining() }).also { out ->
                            var pos = 0
                            bufs.forEach { buf ->
                                val chunk = ByteArray(buf.remaining())
                                buf.get(chunk)
                                chunk.copyInto(out, pos)
                                pos += chunk.size
                            }
                        }
                    }
                assertThat(result).isEqualTo(bytes)
            }
            .verifyComplete()
    }

    @Test
    fun `getContentWithMetadata streams multiple chunks`() {
        val chunk1 = ByteBuffer.wrap("AAA".toByteArray())
        val chunk2 = ByteBuffer.wrap("BBB".toByteArray())
        mockGetObject(responsePublisher(getResponse("text/plain"), listOf(chunk1, chunk2)))

        StepVerifier.create(storage.getContentWithMetadata("multi"))
            .assertNext { cwm ->
                assertThat(cwm.metadata).isEqualTo("text/plain")
                val count = cwm.content.count().block()!!
                assertThat(count).isEqualTo(2)
            }
            .verifyComplete()
    }

    @Test
    fun `getContentWithMetadata returns empty on 404`() {
        mockGetObjectError(s3Error(404))

        StepVerifier.create(storage.getContentWithMetadata("missing-key")).verifyComplete()
    }

    @Test
    fun `getContentWithMetadata propagates non-404 S3 errors`() {
        mockGetObjectError(s3Error(503, "Service Unavailable"))

        StepVerifier.create(storage.getContentWithMetadata("some-key")).expectError(S3Exception::class.java).verify()
    }

    // endregion

    // region generatePresignedUrl()

    @Test
    fun `generatePresignedUrl returns url from presigner`() {
        val presigned = mockk<PresignedGetObjectRequest>()
        every { presigned.url() } returns URI.create("https://s3.example.com/test-bucket/key?signed=1").toURL()
        every { s3Presigner.presignGetObject(any<GetObjectPresignRequest>()) } returns presigned

        StepVerifier.create(storage.generatePresignedUrl("key"))
            .assertNext { assertThat(it).isEqualTo("https://s3.example.com/test-bucket/key?signed=1") }
            .verifyComplete()
    }

    // endregion

    // region prefixed()

    @Test
    fun `prefixed lists objects and maps keys via buildKey`() {
        every { s3Client.listObjectsV2Paginator(any<ListObjectsV2Request>()) } returns
            listPublisher("books/savchenko/problems/1.1.1/img1.png", "books/savchenko/problems/1.1.1/img2.png")

        StepVerifier.create(storage.prefixed("books/savchenko/problems/1.1.1/"))
            .assertNext { assertThat(it).isEqualTo("books/savchenko/problems/1.1.1/img1.png") }
            .assertNext { assertThat(it).isEqualTo("books/savchenko/problems/1.1.1/img2.png") }
            .verifyComplete()

        verify {
            s3Client.listObjectsV2Paginator(
                match<ListObjectsV2Request> {
                    it.bucket() == "test-bucket" && it.prefix() == "books/savchenko/problems/1.1.1/"
                }
            )
        }
    }

    @Test
    fun `prefixed returns empty flux when no objects match`() {
        every { s3Client.listObjectsV2Paginator(any<ListObjectsV2Request>()) } returns listPublisher()

        StepVerifier.create(storage.prefixed("nonexistent/")).verifyComplete()
    }

    // endregion

    // region upload()

    @Test
    fun `upload delegates to putObject and returns key`() {
        every { s3Client.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>()) } returns
            CompletableFuture.completedFuture(PutObjectResponse.builder().build())

        val content = Flux.just(ByteBuffer.wrap("data".toByteArray()))

        StepVerifier.create(storage.upload("my-key", 4L, "text/plain", content)).expectNext("my-key").verifyComplete()

        verify {
            s3Client.putObject(
                match<PutObjectRequest> {
                    it.bucket() == "test-bucket" &&
                        it.key() == "my-key" &&
                        it.contentType() == "text/plain" &&
                        it.contentLength() == 4L
                },
                any<AsyncRequestBody>(),
            )
        }
    }

    @Test
    fun `upload convenience overload collects content and computes size`() {
        every { s3Client.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>()) } returns
            CompletableFuture.completedFuture(PutObjectResponse.builder().build())

        val content = Flux.just(ByteBuffer.wrap("abc".toByteArray()), ByteBuffer.wrap("de".toByteArray()))

        StepVerifier.create(storage.upload("auto-size", "text/plain", content)).expectNext("auto-size").verifyComplete()

        verify { s3Client.putObject(match<PutObjectRequest> { it.contentLength() == 5L }, any<AsyncRequestBody>()) }
    }

    // endregion

    // region delete()

    @Test
    fun `delete returns true on success`() {
        mockDeleteSuccess()

        StepVerifier.create(storage.delete("some-key")).expectNext(true).verifyComplete()
    }

    @Test
    fun `delete returns false on error`() {
        every { s3Client.deleteObject(any<DeleteObjectRequest>()) } returns
            CompletableFuture.failedFuture(RuntimeException("connection refused"))

        StepVerifier.create(storage.delete("some-key")).expectNext(false).verifyComplete()
    }

    // endregion

    // region deleteAll()

    @Test
    fun `deleteAll sends batch request and returns true on success`() {
        val response = mockk<DeleteObjectsResponse>()
        every { response.hasErrors() } returns false
        every { s3Client.deleteObjects(any<DeleteObjectsRequest>()) } returns CompletableFuture.completedFuture(response)

        StepVerifier.create(storage.deleteAll(listOf("key1", "key2", "key3"))).expectNext(true).verifyComplete()

        verify {
            s3Client.deleteObjects(
                match<DeleteObjectsRequest> {
                    it.delete().objects().map { obj -> obj.key() } == listOf("key1", "key2", "key3")
                }
            )
        }
    }

    @Test
    fun `deleteAll returns false when response has errors`() {
        val response = mockk<DeleteObjectsResponse>()
        every { response.hasErrors() } returns true
        every { s3Client.deleteObjects(any<DeleteObjectsRequest>()) } returns CompletableFuture.completedFuture(response)

        StepVerifier.create(storage.deleteAll(listOf("key1"))).expectNext(false).verifyComplete()
    }

    // endregion

    // region move()

    @Test
    fun `move copies then deletes source`() {
        every { s3Client.copyObject(any<CopyObjectRequest>()) } returns
            CompletableFuture.completedFuture(CopyObjectResponse.builder().build())
        mockDeleteSuccess()

        StepVerifier.create(storage.move("source", "target")).expectNext(true).verifyComplete()

        verify {
            s3Client.copyObject(match<CopyObjectRequest> { it.sourceKey() == "source" && it.destinationKey() == "target" })
        }
        verify { s3Client.deleteObject(match<DeleteObjectRequest> { it.key() == "source" }) }
    }

    @Test
    fun `move does not delete source when copy fails`() {
        every { s3Client.copyObject(any<CopyObjectRequest>()) } returns
            CompletableFuture.failedFuture(s3Error(500, "copy failed"))

        StepVerifier.create(storage.move("source", "target")).expectError(S3Exception::class.java).verify()

        verify(exactly = 0) { s3Client.deleteObject(any<DeleteObjectRequest>()) }
    }

    // endregion
}
