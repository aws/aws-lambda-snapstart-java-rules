// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import java.util.UUID;

public class LambdaUsingUuid implements RequestHandler<Map<String,String>, String> {

    private static final String ID1 = "a" + UUID.randomUUID(); // This is a bug
    private static final String ID2 = UUID.randomUUID() + "a"; // This is a bug
    private final String id3;
    private final String id4 = newString(); // This is a bug
    private final UUID id5;
    private final String name = "example"; // This is NOT a bug


    public LambdaUsingUuid() {
        id3 = UUID.randomUUID().toString(); // This is a bug
        id5 = UUID.randomUUID(); // This is a bug
    }

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        return ID1 + ID2 + id3 + id4 + name;
    }

    public String newString() {
        return UUID.randomUUID() + "c";
    }
}
