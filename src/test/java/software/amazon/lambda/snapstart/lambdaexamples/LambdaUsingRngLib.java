// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class LambdaUsingRngLib implements RequestHandler<Map<String,String>, String> {

    private static final RngLib staticRngLib = new RngLib(); // This is a bug (but currently we don't catch this)
    private final RngLib rngLib;
    private final int randomUserId;
    private final int randomLogId;

    public LambdaUsingRngLib() {
        rngLib = new RngLib(); // This is a bug (but currently we don't catch this)
        DeepRngLib deepRngLib = new DeepRngLib();
        randomUserId = deepRngLib.randomInt(); // This is a bug
        randomLogId = rngLib.randomInt(); // This is a bug
    }

    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        // use fields
        return Integer.toString(rngLib.randomInt())
                + Integer.toString(staticRngLib.randomInt())
                + Integer.toString(randomUserId)
                + Integer.toString(randomLogId);
    }
}
