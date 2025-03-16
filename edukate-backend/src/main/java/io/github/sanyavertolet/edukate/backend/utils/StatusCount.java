package io.github.sanyavertolet.edukate.backend.utils;

import io.github.sanyavertolet.edukate.backend.entities.Submission;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record StatusCount(
        Submission.Status status,
        Long count
) {
    public static Map<Submission.Status, Long> asMap(List<StatusCount> list) {
        return list.stream().collect(Collectors.toMap(StatusCount::status, StatusCount::count));
    }
}
