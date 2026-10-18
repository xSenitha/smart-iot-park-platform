package com.smartpark.ingestion.common.dto;

import java.util.List;

public record ValidationResult(
    boolean isValid,
    List<String> errors
) {
    public static ValidationResult success() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors);
    }
}
