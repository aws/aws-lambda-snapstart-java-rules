package software.amazon.lambda.snapstart;

import java.util.ArrayList;
import java.util.List;

public class LambdaHandlerParentsDatabase {

    private final List<String> parentClasses = new ArrayList<>();

    public List<String> getParentClasses() {
        return this.parentClasses;
    }

    public void addLambdaParentClass(String parentClass) {
        this.parentClasses.add(parentClass);
    }
}
