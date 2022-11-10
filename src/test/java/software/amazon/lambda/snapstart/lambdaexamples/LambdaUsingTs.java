package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class LambdaUsingTs implements RequestHandler<Map<String,String>, String> {

    private static String logName;

    public LambdaUsingTs() {
        logName = getLogName(); // This is a bug
    }

    private String getLogName() {
        return "my-app.log." + System.currentTimeMillis();
    }

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        return logName;
    }
}
