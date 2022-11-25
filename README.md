# AWS Lambda SnapStart Bug Scanner

SnapStart Bug Scanner is the [SpotBugs](https://spotbugs.github.io/) plugin for helping AWS Lambda customers inspect
their functions against potential bugs unique to AWS Lambda SnapStart environment.

## How to use

Following sections explain how to enable this plugin in your Gradle and Maven projects.

### Gradle Builds

After SpotBugs is [enabled in the Gradle project](https://spotbugs.readthedocs.io/en/latest/gradle.html) declaring a dependency on SnapStart bug scanner is sufficient. 

Example:

```kotlin
plugins {
    id("com.github.spotbugs") version "4.7.1"
}

spotbugs {
    ignoreFailures.set(false)
    showStackTraces.set(true)
}

dependencies {
    spotbugs("com.github.spotbugs:spotbugs:4.7.1")
    spotbugsPlugins("software.amazon.lambda.snapstart:aws-lambda-snapstart-java-rules:0.1")
}
```

### Maven Builds

After SpotBugs is [enabled in the Maven project](https://spotbugs.readthedocs.io/en/latest/maven.html) declaring a dependency on SnapStart bug scanner is sufficient.

Example:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>${spotbugs.version}</version>
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

