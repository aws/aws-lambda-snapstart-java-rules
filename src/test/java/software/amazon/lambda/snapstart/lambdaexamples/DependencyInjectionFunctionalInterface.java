package software.amazon.lambda.snapstart.lambdaexamples;

import java.util.function.Function;

public class DependencyInjectionFunctionalInterface implements Function<String, String> {

    private final MyDependency myDependency;

    public DependencyInjectionFunctionalInterface(MyDependency myDependency) {
        this.myDependency = myDependency;
    }

    @Override
    public String apply(String s) {
        return myDependency.toString();
    }
}
