package io.github.sanyavertolet.edukate.backend.entities.files;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("problem")
@JsonTypeName("problem")
@EqualsAndHashCode(callSuper = true, of = {"problemId"})
public class ProblemFileKey extends FileKey {
    @Getter
    private final String problemId;

    @PersistenceCreator
    public ProblemFileKey(String problemId, String fileName) {
        super(fileName);
        this.problemId = problemId;
    }

    public static ProblemFileKey of(String problemId, String fileName) {
        return new ProblemFileKey(problemId, fileName);
    }

    private static String constructKey(String problemId, String fileName) {
        return String.format("problems/%s/%s", problemId, fileName);
    }

    public static String prefix(String problemId) {
        return constructKey(problemId, "");
    }

    @Override
    public String toString() {
        return constructKey(problemId, fileName);
    }
}
