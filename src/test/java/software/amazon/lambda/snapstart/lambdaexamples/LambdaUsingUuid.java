package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import java.util.UUID;

public class LambdaUsingUuid implements RequestHandler<Map<String,String>, String> {

    private static final String ID1 = "a" + UUID.randomUUID(); // this is a bug
    private static final String ID2 = UUID.randomUUID() + "a"; // this is a bug
    private final String id3;
    private final String id4 = newString(); // this is a bug
    private final UUID id5;
    private final String name = "example"; // this is NOT a bug


    public LambdaUsingUuid() {
        id3 = UUID.randomUUID().toString(); // this is a bug
        id5 = UUID.randomUUID(); // this is a bug
    }

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        return ID1 + ID2 + id3 + id4 + name;
    }

    public String newString() {
        return UUID.randomUUID().toString() + "c";
    }
}
