
# Ortto Native Android SDK


```java 
// Set up firebase
FirebaseApp.initializeApp(app);
FirebaseMessaging.getInstance().setAutoInitEnabled(true);

// Create an Ortto SDK config object
OrttoConfig config = new OrttoConfig(
    "<DATASOURCE_ID>",
    "<ENDPOINT>"
);

// Start the Ortto service
Ortto.instance().init(config, app);
```


## Publishing

Read this gist by the folks at stream.io https://gist.github.com/zsmb13/56ed98c8fe916de441f2a9d8e060cd4a

Gradle documentation: https://docs.gradle.org/current/userguide/build_environment.html 

GPG information https://github.com/sbt/sbt-ci-release#gpg 

Will publish to this folder https://repo1.maven.org/maven2/com/ortto/androidsdk/


## Using

```groovy
dependencies {
    implementation "com.ortto:androidsdk:<version>"
}
```