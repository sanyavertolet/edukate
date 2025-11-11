package io.github.sanyavertolet.edukate.checker.domain;

import org.springframework.ai.content.Media;

import java.util.List;

public record RequestContext(String problemText, List<Media> problemImages, List<Media> submissionImages) { }
