// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.Location;
import edu.umd.cs.findbugs.ba.MissingClassException;
import edu.umd.cs.findbugs.ba.SignatureConverter;
import edu.umd.cs.findbugs.ba.ca.Call;
import edu.umd.cs.findbugs.ba.ca.CallList;
import edu.umd.cs.findbugs.ba.ca.CallListDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.DescriptorFactory;
import edu.umd.cs.findbugs.classfile.Global;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.log.Profiler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.ReturnInstruction;

/**
 * This class is a detector implementation which runs in the first pass of analysis. With a very simplistic assumption
 * this identifies whether a method might return a pseudo-random value purely based on the fact that it calls a method
 * that's already known to return a pseudo-random value. A better approach is doing proper dataflow analysis to
 * understand whether the pseudo-random value makes it to the return instruction.
 */
public class BuildRandomReturningMethodsDatabase implements Detector {

    private final BugReporter bugReporter;
    private final BugAccumulator bugAccumulator;
    private final ReturnValueRandomnessPropertyDatabase database;
    private final ByteCodeIntrospector introspector;

    // Transient state
    private ClassContext classContext;
    private Method method;
    private MethodDescriptor methodDescriptor;
    private CallListDataflow callListDataflow;
    private Map<Call, MethodDescriptor> callMethodDescriptorMap;
    private CallGraph callGraph;

    public BuildRandomReturningMethodsDatabase(BugReporter reporter) {
        this.bugReporter = reporter;
        this.bugAccumulator = new BugAccumulator(reporter);
        database = new ReturnValueRandomnessPropertyDatabase();
        Global.getAnalysisCache().eagerlyPutDatabase(ReturnValueRandomnessPropertyDatabase.class, database);
        callGraph = new CallGraph();
        callMethodDescriptorMap = new HashMap<>();
        introspector = new ByteCodeIntrospector();
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        this.classContext = classContext;

        String currentMethod = null;
        List<Method> methodsInCallOrder = classContext.getMethodsInCallOrder();
        for (Method method : methodsInCallOrder) {
            try {
                if (method.isAbstract() || method.isNative() || method.getCode() == null) {
                    continue;
                }
                currentMethod = SignatureConverter.convertMethodSignature(classContext.getJavaClass(), method);
                analyzeMethod(method);
            } catch (MissingClassException e) {
                bugReporter.reportMissingClass(e.getClassNotFoundException());
            } catch (DataflowAnalysisException | CFGBuilderException e) {
                bugReporter.logError("While analyzing " + currentMethod + ": BuildRandomReturningMethodsDatabase caught an exception", e);
            }
            bugAccumulator.reportAccumulatedBugs();
        }
    }

    private void analyzeMethod(Method method) throws DataflowAnalysisException, CFGBuilderException {
        if ((method.getAccessFlags() & Const.ACC_BRIDGE) != 0) {
            return;
        }

        this.method = method;
        this.methodDescriptor = DescriptorFactory.instance().getMethodDescriptor(classContext.getJavaClass(), method);
        callListDataflow = classContext.getCallListDataflow(method);

        checkInvokeAndReturnInstructions();
    }

    private void checkInvokeAndReturnInstructions() {
        Profiler profiler = Global.getAnalysisCache().getProfiler();
        profiler.start(BuildRandomReturningMethodsDatabase.class);
        try {
            for (Iterator<Location> i = classContext.getCFG(method).locationIterator(); i.hasNext();) {
                Location location = i.next();
                Instruction ins = location.getHandle().getInstruction();

                if (ins instanceof ReturnInstruction) {
                    examineReturnInstruction(location);
                } else if (ins instanceof InvokeInstruction) {
                    examineInvokeInstruction((InvokeInstruction) ins);
                }
            }
        } catch (CheckedAnalysisException e) {
            AnalysisContext.logError("error:", e);
        } finally {
            profiler.end(BuildRandomReturningMethodsDatabase.class);
        }
    }

    private void examineInvokeInstruction(InvokeInstruction inv) {
        ConstantPoolGen cpg = classContext.getConstantPoolGen();
        MethodDescriptor md = new MethodDescriptor(inv, classContext.getConstantPoolGen());
        Call call = new Call(inv.getClassName(cpg), inv.getName(cpg), inv.getSignature(cpg));
        callMethodDescriptorMap.put(call, md);
    }

    private void examineReturnInstruction(Location location) throws DataflowAnalysisException {
        // We have a crude assumption here that is any method call effects the return value of the caller method.
        CallList callList = callListDataflow.getFactAtLocation(location);
        if (!callList.isValid()) {
            return;
        }

        Iterator<Call> callIterator = callList.callIterator();
        while (callIterator.hasNext()) {
            Call call = callIterator.next();
            MethodDescriptor caller = methodDescriptor;
            MethodDescriptor called = callMethodDescriptorMap.get(call);
            if (called != null) {
                recordCalledMethod(caller, called);
            }
        }
    }

    private void recordCalledMethod(MethodDescriptor caller, MethodDescriptor called) {
        Boolean returnsRandom = database.getProperty(called);
        if (returnsRandom != null && returnsRandom) {
            callGraph.flushCallersToDatabase(caller, database, true);
        } else {
            if (callGraph.isInCallGraph(caller) || isLambdaHandlerInitMethod()) {
                // Call chain from Lambda event handler checks out
                callGraph.record(caller, called);
            }
        }
    }

    private boolean isLambdaHandlerInitMethod() {
        if (introspector.isLambdaHandler(classContext.getXClass())) {
            return Const.STATIC_INITIALIZER_NAME.equals(method.getName())
                    || Const.CONSTRUCTOR_NAME.equals(method.getName());
        }
        return false;
    }

    @Override
    public void report() {
        // this is a non-reporting detector
    }
}
