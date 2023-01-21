package software.amazon.lambda.snapstart.lambdaexamples;

import java.util.UUID;
import java.util.function.Function;

public class ImplementsFunctionalInterface implements Function<String, String> {

    private final UUID random = UUID.randomUUID();

    @Override
    public String apply(String s) {
        return random.toString();
    }
}