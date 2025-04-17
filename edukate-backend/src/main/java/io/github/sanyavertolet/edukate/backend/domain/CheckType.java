package io.github.sanyavertolet.edukate.backend.domain;

public enum CheckType {
    SUPERVISOR,
    SELF,
    ;

    public static CheckType defaultCheckType() {
        return SELF;
    }
}
