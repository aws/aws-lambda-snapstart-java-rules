package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ByteCodeIntrospector {

    private static final String LAMBDA_HANDLER_SIGNATURE = "(Ljava/util/Map;Lcom/amazonaws/services/lambda/runtime/Context;)";
    private static final String LAMBDA_STREAMING_HANDLER_SIGNATURE = "(Ljava/io/InputStream;Ljava/io/OutputStream;Lcom/amazonaws/services/lambda/runtime/Context;)";
    private static final Set<String> LAMBDA_HANDLER_INTERFACES = new HashSet<String>() {{
        add("com.amazonaws.services.lambda.runtime.RequestHandler");
        add("com.amazonaws.services.lambda.runtime.RequestStreamHandler");
    }};

    private static final Map<String, Set<String>> PSEUDO_RANDOM_METHODS = new HashMap<String, Set<String>>() {{
        put("java.lang.Math", setOf("random"));
        put("java.util.UUID", setOf("randomUUID"));
        put("java.util.Random", setOf("nextInt"));
        put("java.util.Random", setOf("nextInt"));
    }};

    private static Set<String> setOf(String ... strings) {
        Set<String> set = new HashSet<>();
        Collections.addAll(set, strings);
        return set;
    };

    boolean isLambdaHandler(XClass xClass) {
        return implementsLambdaInterface(xClass) || hasLambdaHandlerMethod(xClass);
    }

    boolean hasLambdaHandlerMethod(XClass xClass) {
        List<? extends XMethod> methods = xClass.getXMethods();
        for (XMethod method : methods) {
            if (method.getSignature() == null) {
                continue;
            }

            if (method.getSignature().startsWith(LAMBDA_HANDLER_SIGNATURE)) {
                return true;
            }

            if (method.getSignature().startsWith(LAMBDA_STREAMING_HANDLER_SIGNATURE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This returns true only when the class directly implements
     * <a href="https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html">AWS Lambda handler interfaces</a>.
     */
    boolean implementsLambdaInterface(XClass xClass) {
        try {
            ClassDescriptor[] interfaces = xClass.getInterfaceDescriptorList();
            for (ClassDescriptor id : interfaces) {
                if (id.getXClass().isInterface() && LAMBDA_HANDLER_INTERFACES.contains(id.getDottedClassName())) {
                    return true;
                }
            }
        } catch (CheckedAnalysisException e) {
            // ignore
        }
        return false;
    }

    /**
     * This can only return when the method is one of these:
     * 1. {@link Math#random()}
     * 2. {@link UUID#randomUUID()}
     */
    boolean isPseudoRandomMethod(XMethod xMethod) {
        Set<String> classMethods = PSEUDO_RANDOM_METHODS.get(xMethod.getClassName());
        if (classMethods != null) {
            return classMethods.contains(xMethod.getName());
        }
        return false;
    }
}
