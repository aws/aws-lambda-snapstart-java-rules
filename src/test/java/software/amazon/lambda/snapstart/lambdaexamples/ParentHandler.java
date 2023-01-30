package software.amazon.lambda.snapstart.lambdaexamples;

import java.util.UUID;

public abstract class ParentHandler extends SuperParentHandler {
    protected final UUID parentId = UUID.randomUUID();

    protected ParentHandler() {}
}
