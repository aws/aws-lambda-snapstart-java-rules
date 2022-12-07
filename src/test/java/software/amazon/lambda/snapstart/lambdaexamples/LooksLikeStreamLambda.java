// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;

public class LooksLikeStreamLambda {
    private static final UUID LOG_ID = UUID.randomUUID(); // This is a bug

    public void handlesTheEvent(InputStream input, OutputStream output, Context context) {
        PrintWriter pw = new PrintWriter(output);
        pw.println(LOG_ID);
        pw.flush();
    }
}
