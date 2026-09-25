# Making Requests with the Ortto SDK

## Request Queueing and Async API

The Ortto SDK ensures that all state-mutating HTTP requests (such as user identification and device token registration) are processed **one at a time** using an internal request queue. This prevents race conditions and ensures consistent session tracking.

All major request methods now return `CompletableFuture` objects, allowing you to use modern async patterns. Legacy listener-based APIs are still supported for backward compatibility.

---

## Key Methods

- `identify(UserID identifier)`
- `dispatchIdentifyRequest()`
- `dispatchPushRequest()`
- `registerDeviceToken(String token)`

All of these methods:
- **Return a `CompletableFuture<Void>`** (or similar), which completes when the request is finished (success or failure).
- **Are automatically queued**: requests are processed one at a time, in the order they are called.
- **Continue processing** even if a request fails (failures do not block the queue).

---

## Usage Examples

### Using CompletableFuture (Recommended)

```java
// Identify a user
ortto.identify(new UserID("user-123")).thenRun(() -> {
    System.out.println("User identified!");
}).exceptionally(error -> {
    System.err.println("Failed to identify: " + error.getMessage());
    return null;
});

// Register a device token
ortto.registerDeviceToken(token).thenRun(() -> {
    System.out.println("Device token registered!");
}).exceptionally(error -> {
    System.err.println("Failed to register token: " + error.getMessage());
    return null;
});

// Dispatch identify and push requests in sequence
ortto.dispatchIdentifyRequest()
    .thenCompose(v -> ortto.dispatchPushRequest())
    .thenRun(() -> System.out.println("Both requests complete!"));
```

### Using Listener-based APIs (Legacy)

```java
ortto.identify(new UserID("user-123"), new Ortto.OnIdentifyListener() {
    @Override
    public void onComplete() {
        System.out.println("User identified!");
    }
    @Override
    public void onError(Throwable error) {
        System.err.println("Failed to identify: " + error.getMessage());
    }
});

ortto.registerDeviceToken(token, new Ortto.OnTokenRegisteredListener() {
    @Override
    public void onComplete() {
        System.out.println("Device token registered!");
    }
});
```

---

## How the Queue Works

- All requests are placed on a singleton queue inside the SDK.
- Only one request is processed at a time; others wait their turn.
- If a request fails, the queue continues to the next request.
- This prevents issues with session ID and user tracking when multiple requests are made in quick succession.

---

## Best Practices

- Prefer the `CompletableFuture` API for new code.
- Chain requests using `thenCompose` or `thenRun` to ensure order if needed.
- You do **not** need to manually manage the queue; the SDK handles it for you.

---

For more details, see the Ortto SDK JavaDocs or source code.
