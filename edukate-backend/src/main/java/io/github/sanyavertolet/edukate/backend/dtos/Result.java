package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.backend.entities.Problem;

import java.util.List;

public record Result(
        String id,
        String text,
        String notes,
        Problem.ResultType type,
        List<String> images
) {
    public Result {
        if (images == null) {
            images = List.of();
        }
    }

    public Result withImages(List<String> images) {
        return new Result(id, text, notes, type, images);
    }
}
