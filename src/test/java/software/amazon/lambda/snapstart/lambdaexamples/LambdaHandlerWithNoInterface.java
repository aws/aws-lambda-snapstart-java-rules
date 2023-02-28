package software.amazon.lambda.snapstart.lambdaexamples;

import java.util.UUID;

public class LambdaHandlerWithNoInterface {

    private final UUID random = UUID.randomUUID();

    public String handleRequest(){
        return random.toString();
    }
}
