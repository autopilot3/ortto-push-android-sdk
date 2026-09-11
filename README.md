
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

Read this gist by the folks at stream.io https://gist.github.com/zsmb13/56ed98c8fe916de441f2a9d8e060cd4a

Gradle documentation: https://docs.gradle.org/current/userguide/build_environment.html 

GPG information https://github.com/sbt/sbt-ci-release#gpg 

Will publish to this folder https://repo1.maven.org/maven2/com/ortto/androidsdk/

Log into Nexus Repository Manager https://s01.oss.sonatype.org/#welcome 

1. Update messaging module build.gradle `PUBLISH_VERSION` with latest version number you wish to be published
2. Run ` ./gradlew publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository`

## Using

```groovy
dependencies {
    implementation "com.ortto:androidsdk:<version>"
}
```

## Demo application

The in-repository [`demo`](demo/) module is a complete Material 3 reference app for the SDK candidate in this checkout. It covers identity, Android notification permission, FCM token registration and redispatch, real notification delivery and tap routing, tracked links, per-tab screen events, optional in-app widgets, and sanitized diagnostics.

See [`demo/README.md`](demo/README.md) for configuration, automated tests, and the end-to-end validation flow. Real Ortto and Firebase configuration files stay local and are gitignored.
