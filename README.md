# AWS Lambda SnapStart Bug Scanner

SnapStart Bug Scanner is the [SpotBugs](https://spotbugs.github.io/) plugin for helping AWS Lambda customers inspect
their functions against potential bugs unique to AWS Lambda SnapStart environment.

## How to use

Following sections explain how to enable this plugin in your Gradle and Maven projects.

| :exclamation:  Maven repository may not have the artifact available by the time you're reading these instructions. Until it's available, you can clone this repository to your local and run `mvn install` to install this SpotBugs plugin to your local Maven repository. After that you can continue with the setup instructions below.|
|-----------------------------------------|

### Gradle Builds

After SpotBugs is [enabled in the Gradle project](https://spotbugs.readthedocs.io/en/latest/gradle.html) declaring a dependency on SnapStart bug scanner is sufficient. 

Example:

```kotlin
plugins {
    id("com.github.spotbugs") version "4.7.3"
}

spotbugs {
    ignoreFailures.set(false)
    showStackTraces.set(true)
}

dependencies {
    spotbugs("com.github.spotbugs:spotbugs:4.7.3")
    spotbugsPlugins("software.amazon.lambda.snapstart:aws-lambda-snapstart-java-rules:0.1")
}
```

After updating the `build.gradle` file you can run `./gradlew check` to run the analysis and see the result.

### Maven Builds

After SpotBugs is [enabled in the Maven project](https://spotbugs.readthedocs.io/en/latest/maven.html) declaring a dependency on SnapStart bug scanner is sufficient.

Example:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.7.3.0</version>
            <configuration>
                <effort>Max</effort>
                <threshold>medium</threshold>
                <failOnError>true</failOnError>
                <plugins>
                    <plugin>
                        <groupId>software.amazon.lambda.snapstart</groupId>
                        <artifactId>aws-lambda-snapstart-java-rules</artifactId>
                        <version>0.1</version>
                    </plugin>
                </plugins>
            </configuration>
        </plugin>
    </plugins>
</build>
```

After updating `pom.xml`  you can run `mvn compile && mvn spotbugs:spotbugs` to run the analysis and see results in `targets/spotbugsXml.xml` file. Also, you can run `mvn spotbugs:check` to see results on your terminal and `mvn spotbugs:gui` on SpotBug's graphical UI.

## Bug Descriptions

### SNAP_START: Detected handler state that is potentially not resilient to VM snapshot and restore operations. (AWS_LAMBDA_SNAP_START_BUG)

Our analysis shows that AWS Lambda handler class initialization creates state that might have adverse effects
on the output of the function when it uses SnapStart. Lambda functions that use SnapStart are
snapshotted at their initialized state and all execution environments created afterwards share the same initial
state. This means that if the Lambda function relies on state that is not resilient to snapshot and restore
operations, it might manifest an unexpected behavior by using SnapStart.

Note that there are countless ways of initializing a Lambda function handler such that it’s not compatible
with SnapStart. This tool helps you as much as possible but please use your own judgement to and refer
to [the documentation](https://github.com/aws/aws-lambda-snapstart-java-rules/wiki) for
understanding how to avoid making your Lambda function SnapStart incompatible.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

