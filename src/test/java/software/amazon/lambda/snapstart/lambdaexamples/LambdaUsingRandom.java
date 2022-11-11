package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

public class LambdaUsingRandom implements RequestHandler<Map<String,String>, String> {

    private static final Random staticRng = new Random();  // this is a SnapStart bug
    private static final SecureRandom staticSecureRng = new SecureRandom(); // this is not a SnapStart bug
    private final Random rng;

    public LambdaUsingRandom() {
        rng = new Random(0); // this is a SnapStart bug
    }

    @Override
    public String handleRequest(Map<String,String> event, Context context) {
        // return new user id
        return Integer.toString(rng.nextInt()) + Integer.toString(staticRng.nextInt());
    }
}
