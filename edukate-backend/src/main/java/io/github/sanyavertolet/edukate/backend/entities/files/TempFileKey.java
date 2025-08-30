package io.github.sanyavertolet.edukate.backend.entities.files;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;

@JsonTypeName("tmp")
@EqualsAndHashCode(callSuper = true, of = {"userId"})
public class TempFileKey extends FileKey {
    @Getter
    private final String userId;

    @PersistenceCreator
    public TempFileKey(String userId, String fileName) {
        super(fileName);
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
