// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.ba.XField;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.classfile.Global;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;

/**
 * This detector implements a heuristic to detect AWS Lambda functions using the
 */
public class LambdaHandlerInitedWithRandomValue extends OpcodeStackDetector {

    private static final String SNAP_START_BUG = "AWS_LAMBDA_SNAP_START_BUG";

    private final BugReporter bugReporter;
    private boolean isLambdaHandlerClass;
    private boolean isLambdaHandlerParentClass;
    private boolean implementsFunctionalInterface;
    private boolean isLambdaHandlerField;
    private boolean isCracResource;
    private boolean inInitializer;
    private boolean inStaticInitializer;
    private boolean inCracBeforeCheckpoint;
    private ByteCodeIntrospector introspector;
    private ReturnValueRandomnessPropertyDatabase database;

    public LambdaHandlerInitedWithRandomValue(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
        this.isLambdaHandlerClass = false;
        this.isLambdaHandlerParentClass = false;
        this.implementsFunctionalInterface = false;
        this.isLambdaHandlerField = false;
        this.isCracResource = false;
        this.inInitializer = false;
        this.inStaticInitializer = false;
        this.inCracBeforeCheckpoint = false;
        this.introspector = new ByteCodeIntrospector();
    }

    @Override
    public void visit(JavaClass obj) {
        inInitializer = false;
        inStaticInitializer = false;
        inCracBeforeCheckpoint = false;
        XClass xClass = getXClass();
        isLambdaHandlerClass = introspector.isLambdaHandler(xClass);
        isLambdaHandlerParentClass = introspector.isLambdaHandlerParentClass(xClass);
        implementsFunctionalInterface = introspector.implementsFunctionalInterface(xClass);
        isLambdaHandlerField = introspector.isLambdaHandlerField(xClass);
        isCracResource = introspector.isCracResource(xClass);
    }

    @Override
    public boolean shouldVisitCode(Code code) {
        boolean shouldVisit = false;
        if (isLambdaHandlerClass || isLambdaHandlerField || isLambdaHandlerParentClass) {
            inStaticInitializer = getMethodName().equals(Const.STATIC_INITIALIZER_NAME);
            inInitializer = getMethodName().equals(Const.CONSTRUCTOR_NAME);
            database = Global.getAnalysisCache().getDatabase(ReturnValueRandomnessPropertyDatabase.class);
            if (inInitializer || inStaticInitializer) {
                shouldVisit = true;
            }
        } else {
            inStaticInitializer = false;
            inInitializer = false;
        }
        if (isCracResource) {
            inCracBeforeCheckpoint = getMethodName().equals("beforeCheckpoint");
            if (inCracBeforeCheckpoint) {
                shouldVisit = true;
            }
        } else {
            inCracBeforeCheckpoint = false;
        }
        return shouldVisit;
    }

    @Override
    public void sawOpcode(int seen) {
        switch (seen) {
            case Const.PUTSTATIC:
            case Const.PUTFIELD: {
                XField xField = getXFieldOperand();
                if (getXClass().getXFields().contains(xField)) {
                    if (isOperandStackTopBadRng() || isRandomValue() || isOperandStackTopTimestamp()) {
                        reportBug(xField);
                    }
                }
                break;
            }
        }
    }

    private boolean isOperandStackTopBadRng() {
        return introspector.isRandomType(getStack());
    }

    private boolean isRandomValue() {
        XMethod returningMethod = getReturningMethodOrNull();
        if (returningMethod == null) {
            return false;
        }
        Boolean returnsRandom = database.getProperty(returningMethod.getMethodDescriptor());
        return returnsRandom != null && returnsRandom;
    }

    private boolean isOperandStackTopTimestamp() {
        return introspector.isTimestamp(getStack());
    }

    private void reportBug(XField xField) {
        BugInstance bug = new BugInstance(this, SNAP_START_BUG, HIGH_PRIORITY)
                .addClassAndMethod(this.getXMethod())
                .addField(xField)
                .addSourceLine(this);
        bugReporter.reportBug(bug);
    }

    private XMethod getReturningMethodOrNull() {
        OpcodeStack.Item retValItem = getStack().getStackItem(0);
        return retValItem.getReturnValueOf();
    }
}
