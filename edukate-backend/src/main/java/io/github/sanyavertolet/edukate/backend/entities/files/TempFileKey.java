package io.github.sanyavertolet.edukate.backend.entities.files;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("tmp")
@JsonTypeName("tmp")
public class TempFileKey extends FileKey {
    private final String userId;

    private TempFileKey(String userId, String key) {
        super(key);
        this.userId = userId;
    }

    public static TempFileKey of(String userId, String fileName) {
        return new TempFileKey(userId, fileName);
    }

    private static String constructKey(String userId, String fileName) {
        return String.format("users/%s/tmp/%s", userId, fileName);
    }

    public static String prefix(String userId) {
        return constructKey(userId, "");
    }

    @Override
    public String toString() {
        return constructKey(userId, fileName);
    }
}
