package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;
import java.util.UUID;

public class LambdaWithToString implements RequestHandler<Map<String,String>, String> {

    private final String random = UUID.randomUUID().toString();

    @Override
    public String handleRequest(Map<String, String> stringStringMap, Context context) {
        return random;
    }
}
