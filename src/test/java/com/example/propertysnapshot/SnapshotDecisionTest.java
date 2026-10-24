package com.example.propertysnapshot;

import com.example.propertysnapshot.domain.PropertyRecord;
import com.example.propertysnapshot.service.PropertySnapshotService;
import java.time.LocalDate;
import java.util.List;

public final class SnapshotDecisionTest {
    public static void main(String[] args) {
        PropertySnapshotService service = new PropertySnapshotService();
        List<PropertyRecord> input = List.of(new PropertyRecord("mr-1", "maintenance-request", true),
                new PropertyRecord("td-1", "tenant-document", false), new PropertyRecord("ir-1", "inspection-reminder", false));
        String key = service.snapshotKey(LocalDate.of(2026, 10, 23), input);
        if (!"property-snapshots/2026-10-23.json".equals(key)) throw new AssertionError(key);
        if (!service.snapshotKey(LocalDate.of(2026, 10, 23), List.of(new PropertyRecord("td-2", "tenant-document", false))).isEmpty()) throw new AssertionError("unexpected snapshot");
        System.out.println("snapshot-required=true");
    }
}
