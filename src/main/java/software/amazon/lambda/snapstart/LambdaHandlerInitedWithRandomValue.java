package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.ba.XField;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import java.util.HashSet;
import java.util.Set;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

/**
 * This detector implements a heuristic to detect AWS Lambda functions using the
 */
public class LambdaHandlerInitedWithRandomValue extends OpcodeStackDetector {

    private static final String SNAP_START_BUG = "AWS_LAMBDA_SNAP_START_BUG";


    private final BugReporter bugReporter;
    private boolean isLambdaHandlerClass;
    private boolean inInitializer;
    private boolean inStaticInitializer;
    private ByteCodeIntrospector introspector;

    /**
     * These are static or non-static member fields of the Lambda handler class.
     */
    private Set<XField> memberFields;

    public LambdaHandlerInitedWithRandomValue(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
        this.isLambdaHandlerClass = false;
        this.memberFields = new HashSet<>();
        this.inInitializer = false;
        this.inStaticInitializer = false;
        this.introspector = new ByteCodeIntrospector();
    }

    @Override
    public void visit(JavaClass obj) {
        memberFields.clear();
        inInitializer = false;
        inStaticInitializer = false;
        XClass xClass = getXClass();
        isLambdaHandlerClass = introspector.isLambdaHandler(xClass);
    }

    @Override
    public void visit(Field obj) {
        if (isLambdaHandlerClass) {
            XField xField = getXField();
            memberFields.add(xField);
        }
    }

    @Override
    public boolean shouldVisitCode(Code code) {
        if (!isLambdaHandlerClass) {
            return false;
        }
        inStaticInitializer = getMethodName().equals(Const.STATIC_INITIALIZER_NAME);
        inInitializer = getMethodName().equals(Const.CONSTRUCTOR_NAME);
        return inInitializer || inStaticInitializer;
    }

    @Override
    public void sawOpcode(int seen) {
        if (!isLambdaHandlerClass) {
            return;
        }

        switch (seen) {
            case Const.PUTSTATIC:
            case Const.PUTFIELD: {
                XField xField = getXFieldOperand();
                if (memberFields.contains(xField)) {
                    reportIfRandomInitialized(xField);
                }
                break;
            }
        }
    }

    private void reportIfRandomInitialized(XField field) {
        XMethod returningMethod = getReturningMethodOrNull();
        if (returningMethod != null && introspector.isPseudoRandomMethod(returningMethod)) {
            BugInstance bug = new BugInstance(this, SNAP_START_BUG, HIGH_PRIORITY)
                    .addClass(field.getClassDescriptor())
                    .addField(field)
                    .addSourceLine(this);
            bugReporter.reportBug(bug);
        }
    }

    private XMethod getReturningMethodOrNull() {
        OpcodeStack.Item retValItem = getStack().getStackItem(0);
        return retValItem.getReturnValueOf();
    }
}
