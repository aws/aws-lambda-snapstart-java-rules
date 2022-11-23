// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.ba.interproc.MethodPropertyDatabase;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class CallGraph {

    private Map<MethodDescriptor, Set<MethodDescriptor>> callGraph;

    public CallGraph() {
        callGraph = new HashMap<>();
    }

    public boolean isInCallGraph(MethodDescriptor method) {
        return callGraph.containsKey(method);
    }

    public void record(MethodDescriptor caller, MethodDescriptor called) {
        Set<MethodDescriptor> calledBy = callGraph.computeIfAbsent(called, k -> new HashSet<>());
        calledBy.add(caller);
    }

    public void flushCallersToDatabase(MethodDescriptor called,
                                        MethodPropertyDatabase<Boolean> database,
                                        Boolean value) {
        LinkedList<MethodDescriptor> queue = new LinkedList<>();
        queue.push(called);
        while (!queue.isEmpty()) {
            MethodDescriptor m = queue.remove();
            database.setProperty(m, value);
            queueCallers(m, queue);
        }
    }

    private void queueCallers(MethodDescriptor called, LinkedList<MethodDescriptor> queue) {
        Set<MethodDescriptor> callers = callGraph.remove(called);
        if (callers != null) {
            for (MethodDescriptor caller : callers) {
                queue.push(caller);
            }
        }
    }
}
