package io.github.sanyavertolet.edukate.storage.keys;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SubmissionFileKey.class, name = "submission"),
        @JsonSubTypes.Type(value = TempFileKey.class, name = "tmp"),
        @JsonSubTypes.Type(value = ProblemFileKey.class, name = "problem"),
        @JsonSubTypes.Type(value = ResultFileKey.class, name = "result")
})
@ToString
@JsonTypeName("base")
abstract public class FileKey {
    @Getter
    protected String fileName;

    protected FileKey(String fileName) {
        this.fileName = fileName;
    }

    public static String typeOf(FileKey key) {
        if (key instanceof TempFileKey) return "tmp";
        if (key instanceof SubmissionFileKey) return "submission";
        if (key instanceof ProblemFileKey) return "problem";
        if (key instanceof ResultFileKey) return "result";
        return "base";
    }

    public static String ownerOf(FileKey key) {
        if (key instanceof TempFileKey tmp) return tmp.getUserId();
        if (key instanceof SubmissionFileKey sub) return sub.getUserId();
        return null;
    }

    public static FileKey of(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("Key must not be null or blank");
        }
        String norm = rawKey.trim();
        if (norm.startsWith("/")) {
            norm = norm.substring(1);
        }
        norm = norm.replaceAll("/+", "/");
        String[] keySegments = Arrays.stream(norm.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        // users/{userId}/tmp/{fileName}
        if (keySegments.length == 4 && keySegments[0].equals("users") && keySegments[2].equals("tmp")) {
            return TempFileKey.of(keySegments[1], keySegments[3]);
        }

        // users/{userId}/submissions/{problemId}/{submissionId}/{fileName}
        if (keySegments.length == 6 && keySegments[0].equals("users") && keySegments[2].equals("submissions")) {
            return SubmissionFileKey.of(keySegments[1], keySegments[3], keySegments[4], keySegments[5]);
        }

        // problems/{problemId}/{fileName}
        if (keySegments.length == 3 && keySegments[0].equals("problems")) {
            return ProblemFileKey.of(keySegments[1], keySegments[2]);
        }

        // results/{problemId}/{fileName}
        if (keySegments.length == 3 && keySegments[0].equals("results")) {
            return ResultFileKey.of(keySegments[1], keySegments[2]);
        }
        throw new IllegalArgumentException(
                "Invalid key format: '" + rawKey + "' (normalized: '" + String.join("/", keySegments) + "')."
        );
    }
}
