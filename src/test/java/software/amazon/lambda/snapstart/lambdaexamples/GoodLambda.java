// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

class GoodLambda implements RequestHandler<Map<String,String>, String> {

    private final Double userIdDouble;

    public GoodLambda() {
        userIdDouble = 5d;
    }

    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        logger.log("Creating a new user IDs");
        logger.log(Double.toString(userIdDouble));
        return userIdDouble.toString();
    }
}
