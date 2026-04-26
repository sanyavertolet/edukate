package io.github.sanyavertolet.edukate.storage

import java.nio.ByteBuffer
import reactor.core.publisher.Flux

data class ContentWithMetadata<Metadata : Any>(val metadata: Metadata, val content: Flux<ByteBuffer>)
