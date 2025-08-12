package io.github.sanyavertolet.edukate.backend.entities.files;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "files")
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
@JsonTypeName("base")
@TypeAlias("base")
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
abstract public class FileKey {
    @Getter
    protected String fileName;

    @Override
    public String toString() {
        return fileName;
    }

    public static FileKey of(String rawKey) {
        String[] keySegments = rawKey.split("/");

        // /users/{userId}/tmp/{fileName}
        if (keySegments.length == 4 && keySegments[0].equals("users") && keySegments[2].equals("tmp")) {
            return TempFileKey.of(keySegments[1], keySegments[3]);
        }

        // /users/{userId}/submissions/{problemId}/{submissionId}/{fileName}
        if (keySegments.length == 6 && keySegments[0].equals("users") && keySegments[2].equals("submissions")) {
            return SubmissionFileKey.of(keySegments[1], keySegments[3], keySegments[4], keySegments[5]);
        }

        // /problems/{problemId}/{fileName}
        if (keySegments.length == 3 && keySegments[0].equals("problems")) {
            return ProblemFileKey.of(keySegments[1], keySegments[2]);
        }

        // /results/{problemId}/{fileName}
        if (keySegments.length == 3 && keySegments[0].equals("results")) {
            return ResultFileKey.of(keySegments[1], keySegments[2]);
        }
        throw new IllegalArgumentException("Invalid key format: " + rawKey);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileKey && this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
