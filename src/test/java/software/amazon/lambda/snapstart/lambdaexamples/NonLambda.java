package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.Map;
import java.util.UUID;

/**
 * This isn't a Lambda function even though it looks like one.
 */
public class NonLambda {

    private static final UUID STATIC_USER_ID_UUID = UUID.randomUUID();

    private final Double userIdDouble;

    public NonLambda() {
        userIdDouble = Math.random();
    }

    public String handleRequest() {
        return STATIC_USER_ID_UUID.toString();
    }
}
