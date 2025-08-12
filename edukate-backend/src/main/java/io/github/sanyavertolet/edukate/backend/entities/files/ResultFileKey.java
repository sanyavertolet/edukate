package io.github.sanyavertolet.edukate.backend.entities.files;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("result")
@JsonTypeName("result")
public class ResultFileKey extends FileKey {
    private final String problemId;

    private ResultFileKey(String problemId, String fileName) {
        super(fileName);
        this.problemId = problemId;
    }

    public static ResultFileKey of(String problemId, String fileName) {
        return new ResultFileKey(problemId, fileName);
    }

    private static String constructKey(String problemId, String fileName) {
        return String.format("results/%s/%s", problemId, fileName);
    }

    public static String prefix(String problemId) {
        return constructKey(problemId, "");
    }

    @Override
    public String toString() {
        return constructKey(problemId, fileName);
    }
}
