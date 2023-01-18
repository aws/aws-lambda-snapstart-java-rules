// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.ba.interproc.MethodPropertyDatabase;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReturnValueRandomnessPropertyDatabase extends MethodPropertyDatabase<Boolean> {

    private static final Set<MethodDescriptor> ALREADY_KNOWN_PSEUDO_RANDOM_GEN_METHODS = new HashSet<>(Arrays.asList(
            new MethodDescriptor("java/lang/Math", "random", "()D", true),
            new MethodDescriptor("java/util/UUID", "randomUUID", "()Ljava/util/UUID;", true),
            new MethodDescriptor("java/util/UUID", "toString", "()Ljava/lang/String;"),
            new MethodDescriptor("java/util/Random", "nextInt", "()I"),
            new MethodDescriptor("java/lang/StrictMath", "random", "()D", true)
    ));

    public ReturnValueRandomnessPropertyDatabase() {
        super();
        for (MethodDescriptor d : ALREADY_KNOWN_PSEUDO_RANDOM_GEN_METHODS) {
            setProperty(d, true);
        }
    }

    @Override
    protected Boolean decodeProperty(String propStr) {
        return Boolean.parseBoolean(propStr);

    }

    @Override
    protected String encodeProperty(Boolean property) {
        return property.toString();
    }
}
