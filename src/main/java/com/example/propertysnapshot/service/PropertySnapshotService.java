package com.example.propertysnapshot.service;

import com.example.propertysnapshot.domain.PropertyRecord;
import com.example.propertysnapshot.domain.SnapshotDecision;
import java.time.LocalDate;
import java.util.List;

public final class PropertySnapshotService {
    public String snapshotKey(LocalDate date, List<PropertyRecord> records) {
        SnapshotDecision decision = SnapshotDecision.evaluate(records);
        if (!decision.required()) return "";
        return "property-snapshots/" + date + ".json";
    }

    public String json(LocalDate date, List<PropertyRecord> records) {
        StringBuilder body = new StringBuilder("{\"date\":\"").append(date).append("\",\"records\":[");
        for (int i = 0; i < records.size(); i++) {
            PropertyRecord record = records.get(i);
            if (i > 0) body.append(',');
            body.append("{\"id\":\"").append(record.id()).append("\",\"kind\":\"")
                .append(record.kind()).append("\",\"needsAttention\":").append(record.needsAttention()).append('}');
        }
        return body.append("]}").toString();
    }
}
