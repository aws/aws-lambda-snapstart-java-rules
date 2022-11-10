package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** All class members (static-non static) are tainted and will be caught by
 * our rudimentary SnapStart bug detector.
 */
class BadLambda implements RequestHandler<Map<String,String>, String> {

    private static final Random RNG = new Random();
    private static final SecureRandom SEC_RNG = new SecureRandom();

    // These are buggy fields
    private static final UUID STATIC_USER_ID_UUID = UUID.randomUUID();
    private static final Double STATIC_USER_ID_DOUBLE = Math.random();
    private static final Integer STATIC_USER_ID_INT = RNG.nextInt();
    private static final Integer STATIC_USER_ID_INT_SEC = SEC_RNG.nextInt();
    private final UUID userIdUuid = UUID.randomUUID();
    private final Double userIdDouble;

    public BadLambda() {
        userIdDouble = Math.random();
    }

    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        logger.log("Creating a new user IDs");
        logger.log(Double.toString(userIdDouble));
        logger.log(Double.toString(STATIC_USER_ID_DOUBLE));
        logger.log(Double.toString(STATIC_USER_ID_INT));
        logger.log(Double.toString(STATIC_USER_ID_INT_SEC));
        logger.log(userIdUuid.toString());
        return STATIC_USER_ID_UUID.toString();
    }
}
