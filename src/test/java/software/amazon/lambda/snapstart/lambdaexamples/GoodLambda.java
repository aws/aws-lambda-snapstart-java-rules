package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import java.util.UUID;

class GoodLambda implements RequestHandler<Map<String,String>, String> {

    private static final UUID STATIC_USER_ID_UUID = newUUID();
    private static final Double STATIC_USER_ID_DOUBLE = newRandom();

    private final UUID userIdUuid = newUUID();
    private final Double userIdDouble;

    public GoodLambda() {
        userIdDouble = newRandom();
    }

    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        logger.log("Creating a new user IDs");
        logger.log(Double.toString(userIdDouble));
        logger.log(Double.toString(STATIC_USER_ID_DOUBLE));
        logger.log(userIdUuid.toString());
        return STATIC_USER_ID_UUID.toString();
    }

    private static UUID newUUID() {
        return UUID.randomUUID();
    }

    private static Double newRandom() {
        return Math.random();
    }
}
