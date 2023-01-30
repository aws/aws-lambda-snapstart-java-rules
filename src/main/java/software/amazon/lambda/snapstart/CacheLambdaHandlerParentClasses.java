package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.XClass;
import org.apache.bcel.classfile.JavaClass;
import edu.umd.cs.findbugs.classfile.Global;

import java.util.Objects;


public class CacheLambdaHandlerParentClasses implements Detector {

    private final ByteCodeIntrospector introspector;
    private ClassContext classContext;
    private XClass xClass;
    private final LambdaHandlerParentsDatabase database;

    public CacheLambdaHandlerParentClasses(BugReporter bugReporter) {
        this.introspector = new ByteCodeIntrospector();
        database = new LambdaHandlerParentsDatabase();
        Global.getAnalysisCache().eagerlyPutDatabase(LambdaHandlerParentsDatabase.class, database);
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        this.classContext = classContext;
        this.xClass = classContext.getXClass();
        if (introspector.isLambdaHandler(xClass)) {
            JavaClass[] parentClasses = null;
            try {
                parentClasses = classContext.getJavaClass().getSuperClasses();
            } catch (ClassNotFoundException e) {
                // Do nothing
            }

            if (Objects.nonNull(parentClasses)) {
                for (JavaClass parentClass : parentClasses) {
                    if (!parentClass.getClassName().equals("java.lang.Object")) {
                        database.addLambdaParentClass(parentClass.getClassName().replace(".", "/"));
                    }
                }
            }
        }
    }

    @Override
    public void report() {
        // this is a non-reporting detector
    }
}
