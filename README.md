
# Ortto Native Android SDK


```java 
// Set up firebase
FirebaseApp.initializeApp(app);
FirebaseMessaging.getInstance().setAutoInitEnabled(true);

// Create an Ortto SDK config object
OrttoConfig config = new OrttoConfig(
    "<APPLICATION_KEY>",
    "<API_ENDPOINT>"
);

// Start the Ortto service
Ortto.instance().init(config, app);

// Configure Android in-app notifications
import com.ortto.messaging.widget.CaptureConfig;

// in App@onCreate
Ortto.instance().initCapture(new CaptureConfig(
    "<DATASOURCE_ID>",
    "<CAPTURE_JS_URL>",
    "<API_ENDPOINT>"
));
```


## Publishing

Releases are verified locally, then uploaded to the Central Portal as a closed,
user-managed staging deployment. Staging never publishes automatically.

1. Run `./scripts/release-local.sh verify v1.8.10`.
2. Merge the reviewed release commit and create its signed version tag.
3. Export a Central Portal user token as `OSSRH_USERNAME` and `OSSRH_PASSWORD`,
   plus `SIGNING_KEY`, `SIGNING_KEY_ID`, and `SIGNING_PASSWORD`.
4. From the exact clean tag, run `./scripts/release-local.sh stage v1.8.10`.
5. Inspect Central validation and either drop the deployment or publish it from
   the Portal after explicit approval.
6. Wait for Maven Central, build a clean public consumer, then create the GitHub
   release.

## Using

```groovy
dependencies {
    implementation "com.ortto:androidsdk:<version>"
}
```
