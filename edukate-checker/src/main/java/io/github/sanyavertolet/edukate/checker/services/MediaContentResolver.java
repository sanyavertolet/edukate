package io.github.sanyavertolet.edukate.checker.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.content.Media;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class MediaContentResolver {
    private final WebClient webClient;
    private final static MediaType DEFAULT_MEDIA_TYPE = MediaType.IMAGE_JPEG;

    public MediaContentResolver(WebClient.Builder webClientBuilder) {
        webClient = webClientBuilder.build();
    }

    public Flux<Media> resolveMedia(List<String> imageUrls) {
        return Flux.fromIterable(imageUrls)
                // todo: prod could send S3 links instead of fetching the pictures locally
                .flatMap(this::fetchAsBytes)
                .flatMap(this::mediaWithFileContent);
    }

    private Mono<Media> mediaWithFileContent(ResponseEntity<byte[]> fileContentResponse) {
        return Mono.justOrEmpty(fileContentResponse)
                .flatMap(entity -> {
                    MediaType parsedMimeType = entity.getHeaders().getContentType();
                    log.debug("Parsed mime type: {} (NOT USED YET)", parsedMimeType);
                    return Mono.justOrEmpty(entity.getBody())
                            .map(bytes -> Media.builder()
                                    // todo: use parsedMimeType when S3 starts saving mime types
                                    .mimeType(DEFAULT_MEDIA_TYPE)
                                    .data(bytes)
                                    .build()
                            );
                });
    }

    private Mono<ResponseEntity<byte[]>> fetchAsBytes(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .toEntity(byte[].class);
    }
}
