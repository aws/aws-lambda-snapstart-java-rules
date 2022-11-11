package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class LambdaUsingRngLib implements RequestHandler<Map<String,String>, String> {

    private static final RngLib staticRngLib = new RngLib(); // This is SnapStart bug
    private final RngLib rngLib;

    public LambdaUsingRngLib() {
        rngLib = new RngLib(); // This is SnapStart bug
    }

    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        // return new user id
        return Integer.toString(rngLib.randomInt()) + Integer.toString(staticRngLib.randomInt());
    }
}
