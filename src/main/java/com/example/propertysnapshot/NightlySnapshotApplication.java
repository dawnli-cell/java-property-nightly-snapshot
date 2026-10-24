package com.example.propertysnapshot;

import com.example.propertysnapshot.domain.PropertyRecord;
import com.example.propertysnapshot.service.PropertySnapshotService;
import com.example.propertysnapshot.storage.InfraiStorageClient;
import java.time.LocalDate;
import java.util.List;

public final class NightlySnapshotApplication {
    public static void main(String[] args) throws Exception {
        String apiKey = require("INFRAI_API_KEY");
        String bucket = System.getenv().getOrDefault("INFRAI_BUCKET", "property-nightly");
        List<PropertyRecord> records = List.of(new PropertyRecord("mr-104", "maintenance-request", true),
                new PropertyRecord("td-208", "tenant-document", false), new PropertyRecord("ir-031", "inspection-reminder", true));
        PropertySnapshotService service = new PropertySnapshotService();
        String key = service.snapshotKey(LocalDate.now(), records);
        if (key.isEmpty()) return;
        InfraiStorageClient storage = new InfraiStorageClient(apiKey);
        storage.createBucket(bucket);
        try {
            storage.upload(bucket, key, service.json(LocalDate.now(), records));
            System.out.println("snapshot-uploaded=" + key);
        } finally {
            storage.deleteObject(bucket, key);
            storage.deleteBucket(bucket);
        }
    }

    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required");
        return value;
    }
}
