package software.amazon.lambda.snapstart.lambdaexamples;

import java.util.UUID;

public class MyDependency {
    private final UUID random = UUID.randomUUID();

    public UUID getUUID() {
        return random;
    }
}
