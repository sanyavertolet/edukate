package io.github.sanyavertolet.edukate.backend.dtos;

import java.util.List;

public record Result(
        String id,
        String text,
        String notes,
        ResultType type,
        List<String> images
) {
    public Result {
        if (images == null) {
            images = List.of();
        }
    }

    public enum ResultType {
        FORMULA, TEXT, NUMERIC
    }

    public Result withImages(List<String> images) {
        return new Result(id, text, notes, type, images);
    }
}
