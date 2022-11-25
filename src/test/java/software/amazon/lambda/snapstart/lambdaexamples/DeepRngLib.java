package software.amazon.lambda.snapstart.lambdaexamples;

public class DeepRngLib {

    private final RngLib random;

    public DeepRngLib() {
        this.random = new RngLib();
    }

    public int randomInt() {
        return random.randomInt();
    }
}
