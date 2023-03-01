package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LambdaWithParentClass extends ParentHandler implements RequestHandler<String, String> {

    @Override
    public String handleRequest(String s, Context context) {
        Logger logger = Logger.getLogger("LambdaWithParentClass");
        logger.log(Level.INFO, superParentId.toString());
        return parentId.toString(); 
    }
}
