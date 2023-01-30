// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.Global;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ByteCodeIntrospector {

    private static final String LAMBDA_HANDLER_SIGNATURE = "(Ljava/util/Map;Lcom/amazonaws/services/lambda/runtime/Context;)";
    private static final String LAMBDA_STREAMING_HANDLER_SIGNATURE = "(Ljava/io/InputStream;Ljava/io/OutputStream;Lcom/amazonaws/services/lambda/runtime/Context;)";
    private static final Set<String> LAMBDA_HANDLER_INTERFACES = new HashSet<String>() {{
        add("com.amazonaws.services.lambda.runtime.RequestHandler");
        add("com.amazonaws.services.lambda.runtime.RequestStreamHandler");
    }};

    private static final String CRAC_RESOURCE_INTERFACE = "org.crac.Resource";

    private static final String RANDOM_SIGNATURE = "Ljava/util/Random;";

    private static final String INSTANT_SIGNATURE = "Ljava/time/Instant;";

    private static final Map<String, Set<String>> TIMESTAMP_METHODS = new HashMap<String, Set<String>>() {{
        put("java.lang.System", setOf("currentTimeMillis", "nanoTime"));
    }};

    private LambdaHandlerParentsDatabase lambdaHandlerParentsDatabase;

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
        for (ClassDescriptor classDescriptor : xClass.getInterfaceDescriptorList()) {
            try {
                if (classDescriptor.getXClass().isInterface() && LAMBDA_HANDLER_INTERFACES.contains(classDescriptor.getDottedClassName())) {
                    return true;
                }
            } catch (CheckedAnalysisException e) {
                // ignore
            }
        }
        return false;
    }

    boolean isLambdaHandlerParentClass(XClass xClass) {
        lambdaHandlerParentsDatabase = Global.getAnalysisCache().getDatabase(LambdaHandlerParentsDatabase.class);
        return lambdaHandlerParentsDatabase.getParentClasses().contains(xClass.toString());
    }

    /**
     * This returns true only when the class directly implements the CRaC (Coordinated Restore at Checkpoint)
     * <a href="https://javadoc.io/doc/io.github.crac/org-crac/latest/org/crac/Resource.html">Resource interface</a>.
     */
    boolean isCracResource(XClass xClass) {
        for (ClassDescriptor classDescriptor : xClass.getInterfaceDescriptorList()) {
            try {
                if (classDescriptor.getXClass().isInterface()) {
                    if (CRAC_RESOURCE_INTERFACE.equals(classDescriptor.getDottedClassName())) {
                        return true;
                    }
                }
            } catch (CheckedAnalysisException e) {
                // ignore
            }
        }
        return false;
    }

    /**
     * Return true if is {@link Random} type.
     * Otherwise, return false.
     */
    boolean isRandomType(OpcodeStack stack) {
        return RANDOM_SIGNATURE.equals(stack.getStackItem(0).getSignature());
    }

    /**
     * Return true if:
     *  - is Instant type
     *  - is a known method that returns a timestamp-like value, such as {@link System#currentTimeMillis()}
     * Otherwise, return false.
     */
    boolean isTimestamp(OpcodeStack stack) {
        if (INSTANT_SIGNATURE.equals(stack.getStackItem(0).getSignature())) {
            return true;
        }
        XMethod xMethod = stack.getStackItem(0).getReturnValueOf();
        if (xMethod != null) {
            Set<String> methodNames = TIMESTAMP_METHODS.get(xMethod.getClassName());
            if (methodNames != null) {
                return methodNames.contains(xMethod.getName());
            }
        }
        return false;
    }

}
