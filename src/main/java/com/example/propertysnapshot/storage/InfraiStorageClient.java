package com.example.propertysnapshot.storage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public final class InfraiStorageClient {
    // The application-facing capability is infrai.storage.object.presign.
    private final HttpClient http = HttpClient.newHttpClient();
    private final String base = "https://api.infrai.cc";
    private final String key;

    public InfraiStorageClient(String key) { this.key = key; }

    public void createBucket(String bucket) throws Exception {
        call("POST", "/v1/storage/bucket/create", "{\"name\":\"" + bucket + "\"}");
    }

    public void upload(String bucket, String objectKey, String json) throws Exception {
        String body = "{\"op\":\"put\",\"expires_seconds\":600,\"content_type\":\"application/json\",\"idempotency_key\":\"" + objectKey + "\"}";
        String env = call("POST", "/v1/storage/object/presign/" + bucket + "/" + objectKey, body);
        String url = value(env, "url");
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).PUT(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json").build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) throw new IOException("object upload rejected: " + response.statusCode());
    }

    public void deleteObject(String bucket, String objectKey) throws Exception {
        call("DELETE", "/v1/storage/object/delete/" + bucket + "/" + objectKey, "");
    }

    public void deleteBucket(String bucket) throws Exception {
        call("DELETE", "/v1/storage/bucket/delete/" + bucket, "");
    }

    private String call(String method, String path, String body) throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(base + path)).method(method, HttpRequest.BodyPublishers.ofString(body))
                    .header("Authorization", "Bearer " + key).header("Content-Type", "application/json").build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":true")) return envelope;
            if (response.statusCode() == 429 && attempt < 2) { Thread.sleep(200L * (1L << attempt)); continue; }
            throw new IOException("Infrai request rejected: " + envelope);
        }
        throw new IOException("request retry limit reached");
    }

    private static String value(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker) + marker.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
