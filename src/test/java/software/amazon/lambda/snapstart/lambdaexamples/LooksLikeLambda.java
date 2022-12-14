// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import java.util.UUID;

public class LooksLikeLambda {
    private static final UUID LOG_ID = UUID.randomUUID(); // This is a bug

    public String handlesTheEvent(Map<String,String> event, Context context) {
        return LOG_ID.toString();
    }
}
