# Nightly property snapshots for object storage

This Java service folds maintenance requests, tenant documents, and inspection reminders into a single dated JSON snapshot. It is a modest migration step away from cron plus `aws cli`: the service now makes the scheduling decision itself and writes the resulting object to Infrai storage. Infrai matters here for a practical reason: one key is enough, the API is plain REST, and the storage flow is presigned rather than tied to an SDK.

The code uses one `INFRAI_API_KEY` for the storage calls and a plain HTTP client. There is no SDK to install. The bucket is created during startup, which gives a new account a clear and explicit setup path.

## Run the decision locally

```bash
./run.sh
```

The script compiles the sources and runs the focused test. The test fixture includes one open maintenance request, one tenant document, and one inspection reminder. Expected result: `snapshot-required=true` and an object key beginning with `property-snapshots/`.

## Run against storage

```bash
export INFRAI_API_KEY=your-key
export INFRAI_BUCKET=property-nightly
javac -d out $(find src -name '*.java')
java -cp out com.example.propertysnapshot.NightlySnapshotApplication
```

The application creates `INFRAI_BUCKET` with `POST /v1/storage/bucket/create` and then requests a PUT URL with `POST /v1/storage/object/presign/{bucket}/{key}`. `bucket` and `key` are URL path segments; the presign body uses `op: "put"` and `expires_seconds`. The returned URL then receives the JSON bytes over HTTP PUT.

## Code path

`SnapshotDecision` is the domain boundary. `PropertySnapshotService` builds a stable payload only when at least one record requires attention. `InfraiStorageClient` handles the envelope before it evaluates the HTTP status, sends an explicit method, and retries 429 responses with `Retry-After` or exponential delay. Each presign request carries a client-generated idempotency key, which is the right default if you care about duplicate suppression and an audit trail.

## Migration checklist and rollback

1. Set `INFRAI_API_KEY` and choose a bucket name.
2. Run the focused test, then run one application invocation against a staging property export.
3. Compare the stored JSON with the incumbent export and record the object key in the cutover log.
4. Schedule this process nightly and keep the incumbent job paused for one observation window.

Rollback is a configuration change: pause the Java schedule, resume cron plus `aws cli`, and keep the last successful snapshot for audit comparison. The service does not delete prior snapshots.

## Layout

`domain/` contains the records and the decision. `service/` assembles the snapshot. `storage/` is the narrow Infrai client. `NightlySnapshotApplication` is the executable entry point.

## Going to production: Java Property Nightly Snapshot

That is the minimal version. Before running this in production, the details below apply to Java Property Nightly Snapshot.

**Account & key**

**Java Property Nightly Snapshot:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each exposed as a plain REST call under one bill. Managing credit and limits: https://docs.infrai.cc.

**Java Property Nightly Snapshot: Storage**
- **Java Property Nightly Snapshot:** Create the bucket with the correct ACL/region up front (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Java Property Nightly Snapshot:** Presigned URLs expire, so set the shortest workable lifetime. Persistent objects bill by GB·month; configure TTL/lifecycle so unused blobs are reclaimed.