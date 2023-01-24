package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.XClass;
import org.apache.bcel.classfile.Field;

import java.util.HashSet;

/**
 * This detector stores fields with the Lambda Handler and Crac resources to be used later
 * for visiting the classes passed in through dependency injection
 */
public class CacheLambdaHandlerFields implements Detector {

    public static HashSet<String> fieldsToVisit = new HashSet<>();
    private final ByteCodeIntrospector introspector;
    private ClassContext classContext;
    private XClass xClass;

    public CacheLambdaHandlerFields(BugReporter reporter) {
        introspector = new ByteCodeIntrospector();
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        this.classContext = classContext;
        this.xClass = classContext.getXClass();
        if (introspector.isLambdaHandler(xClass)) {
            Field[] fields = classContext.getJavaClass().getFields();
            for (Field field : fields) {
                fieldsToVisit.add(field.getType().toString().replace(".", "/"));
            }
        }
    }

    @Override
    public void report() {
        // this is a non-reporting detector
    }
}
