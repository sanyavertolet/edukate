package io.github.sanyavertolet.edukate.backend.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

@UtilityClass
public final class Sorts {

    public static Sort semVerSort() {
        return semVerSort(SemVerUtils.majorFieldName, SemVerUtils.minorFieldName, SemVerUtils.patchFieldName);
    }

    public static Sort semVerSort(String majorField, String minorField, String patchField) {
        return Sort.by(
                Sort.Order.asc(majorField),
                Sort.Order.asc(minorField),
                Sort.Order.asc(patchField),
                Sort.Order.asc("_id")
        );
    }
}
