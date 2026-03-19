package io.github.sanyavertolet.edukate.checker.domain

import org.springframework.ai.content.Media

data class RequestContext(
    val problemText: String,
    val problemImages: List<Media>,
    val submissionImages: List<Media>
) {
    init {
        require(problemText.isNotBlank()) { "Problem text cannot be blank" }
        // problemImages might be empty
        require(submissionImages.isNotEmpty()) { "Submission images cannot be empty" }
    }
}