// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import static java.lang.System.out;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

public class LambdaUsingTs implements RequestHandler<Map<String,String>, String> {

    private static String logName;

    private long tsFromSystemTimeMillis;
    private long tsFromSystemTimeNano;
    private Instant tsFromInstantNow;
    private Instant tsFromClock;

    public LambdaUsingTs(Clock clock) {
        tsFromSystemTimeMillis = System.currentTimeMillis(); // This is a bug
        tsFromSystemTimeNano = System.nanoTime(); // This is a bug
        tsFromInstantNow = Instant.now(); // This is a bug
        tsFromClock = clock.instant(); // This is a bug

        logName = getLogName(); // This is a bug
    }

    private String getLogName() {
        return "my-app.log." + System.currentTimeMillis();
    }

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        out.println(logName);
        out.println(tsFromSystemTimeMillis);
        out.println(tsFromSystemTimeNano);
        out.println(tsFromInstantNow);
        out.println(tsFromClock);
        return "200";
    }
}
