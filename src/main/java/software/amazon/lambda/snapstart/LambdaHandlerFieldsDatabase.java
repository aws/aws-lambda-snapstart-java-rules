package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.ba.interproc.FieldPropertyDatabase;
import edu.umd.cs.findbugs.ba.interproc.PropertyDatabaseFormatException;

public class LambdaHandlerFieldsDatabase extends FieldPropertyDatabase<Boolean> {

    public LambdaHandlerFieldsDatabase() {
        super();
    }

    @Override
    protected Boolean decodeProperty(String s) throws PropertyDatabaseFormatException {
        return null;
    }

    @Override
    protected String encodeProperty(Boolean aBoolean) {
        return null;
    }

}
