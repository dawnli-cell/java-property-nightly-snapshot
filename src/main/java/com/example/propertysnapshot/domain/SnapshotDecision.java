package com.example.propertysnapshot.domain;

import java.util.List;

public record SnapshotDecision(boolean required, String reason) {
    public static SnapshotDecision evaluate(List<PropertyRecord> records) {
        boolean required = records.stream().anyMatch(PropertyRecord::needsAttention);
        return required
                ? new SnapshotDecision(true, "attention-required")
                : new SnapshotDecision(false, "no-change");
    }
}
