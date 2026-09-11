
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

Releases use the Central Portal OSSRH Staging API compatibility service. The
`OSSRH_USERNAME` and `OSSRH_PASSWORD` values must contain a Central Portal User
Token, not a legacy OSSRH token. GPG signing values are also required for a
Central release.

1. Set the release version through `VERSION_NAME`, for example `VERSION_NAME=1.8.9`.
2. Run `./gradlew clean test lint assembleRelease`.
3. Run `./gradlew publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository`.
4. Verify the deployment in the [Central Publisher Portal](https://central.sonatype.com/publishing) and then in [Maven Central](https://repo1.maven.org/maven2/com/ortto/androidsdk/).

For a credential-free local consumer test, run
`VERSION_NAME=1.8.9 ./gradlew publishReleasePublicationToMavenLocal` and resolve
`com.ortto:androidsdk:1.8.9` from `mavenLocal()` in a separate application.

## Using

```groovy
dependencies {
    implementation "com.ortto:androidsdk:<version>"
}
```
