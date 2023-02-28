package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.classfile.DescriptorFactory;
import edu.umd.cs.findbugs.classfile.FieldDescriptor;
import edu.umd.cs.findbugs.classfile.Global;
import org.apache.bcel.classfile.Field;

/**
 * This detector stores fields with the Lambda Handler and Crac resources to be used later
 * for visiting the classes passed in through dependency injection
 */
public class CacheLambdaHandlerFields implements Detector {

    private final ByteCodeIntrospector introspector;
    private ClassContext classContext;
    private XClass xClass;
    private final LambdaHandlerFieldsDatabase database;


    public CacheLambdaHandlerFields(BugReporter reporter) {
        introspector = new ByteCodeIntrospector();
        database = new LambdaHandlerFieldsDatabase();
        Global.getAnalysisCache().eagerlyPutDatabase(LambdaHandlerFieldsDatabase.class, database);
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        this.classContext = classContext;
        this.xClass = classContext.getXClass();
        if (introspector.isLambdaHandler(xClass)) {
            Field[] fields = classContext.getJavaClass().getFields();
            for (Field field : fields) {
                FieldDescriptor fieldDescriptor = DescriptorFactory.instance().getFieldDescriptor(xClass.toString().replace(".", "/"), field);
                database.setProperty(fieldDescriptor, true);
            }
        }
    }

    @Override
    public void report() {
        // this is a non-reporting detector
    }
}
