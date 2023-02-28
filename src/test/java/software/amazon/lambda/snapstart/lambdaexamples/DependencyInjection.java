package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class DependencyInjection implements RequestHandler<String, String> {
    private final MyDependency myDependency;

    public DependencyInjection(MyDependency myDependency) {
        this.myDependency = myDependency;
    }
    @Override
    public String handleRequest(String s, Context context) {
        return myDependency.getUUID().toString();
    }
}
