package io.github.sanyavertolet.edukate.backend.utils;

import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public final class SemVerUtils {

    /**
     * Warning: changing this field will destroy the sorting logic.
     *
     * @see ProblemRepository#findProblemsWithMissingIndices
     */
    public static String majorFieldName = "majorId";

    /**
     * Warning: changing this field will destroy the sorting logic.
     *
     * @see ProblemRepository#findProblemsWithMissingIndices
     */
    public static String minorFieldName = "minorId";

    /**
     * Warning: changing this field will destroy the sorting logic.
     *
     * @see ProblemRepository#findProblemsWithMissingIndices
     */
    public static String patchFieldName = "patchId";

    private SemVerUtils() { }

    public static Tuple3<Integer, Integer, Integer> parse(String version) {
        String[] segments = version.split("\\.");
        if (segments.length != 3) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }
        return Tuples.of(Integer.parseInt(segments[0]), Integer.parseInt(segments[1]), Integer.parseInt(segments[2]));
    }
}
