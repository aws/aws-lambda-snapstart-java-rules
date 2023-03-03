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
    id("com.github.spotbugs") version "4.7.3"
}

spotbugs {
    ignoreFailures.set(false)
    showStackTraces.set(true)
}

dependencies {
    spotbugs("com.github.spotbugs:spotbugs:4.7.3")
    spotbugsPlugins("software.amazon.lambda.snapstart:aws-lambda-snapstart-java-rules:0.2.1")
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
                        <version>0.2.1</version>
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

Our analysis shows that AWS Lambda handler class initialization creates state that may not remain unique for the function 
when it uses SnapStart. Lambda functions that use SnapStart are  snapshotted at their initialized state and all execution 
environments created afterwards share the same initial state. This means that if the Lambda function relies on state that 
is not resilient to snapshot and restore operations, it might manifest an unexpected behavior by using SnapStart.

This tool helps provide an insight on possible cases where your code may not be fully compatible with 
snapstart enabled. Please verify that your code maintains uniqueness with SnapStart. For best practices, follow the 
guidelines outlined in [SnapStart feature documentation](https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html).
For more information on the tool and examples of scenarios that the tool helps identify, refer to the
[SnapStart scanner GitHub documentation](https://github.com/aws/aws-lambda-snapstart-java-rules/wiki).

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

