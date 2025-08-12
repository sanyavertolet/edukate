package io.github.sanyavertolet.edukate.backend.entities.files;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("problem")
@JsonTypeName("problem")
public class ProblemFileKey extends FileKey {
    private final String problemId;

    private ProblemFileKey(String problemId, String fileName) {
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
