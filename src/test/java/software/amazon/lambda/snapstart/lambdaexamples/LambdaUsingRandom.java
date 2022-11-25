// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

public class LambdaUsingRandom implements RequestHandler<Map<String,String>, String> {

    private static final Random staticRng = new Random();  // This is a bug
    private static final Random staticSecureRng = new SecureRandom(); // This is NOT a bug
    private final Random rng;
    private final Random secureRng;
    private final Random injectedRng;
    private final Random injectedSecureRng;

    public LambdaUsingRandom(Random injectedRng, SecureRandom injectedSecureRng) {
        this.rng = new Random(0); // This is a bug
        this.secureRng = new SecureRandom(); // This is NOT a bug
        this.injectedRng = injectedRng; // This is a bug
        this.injectedSecureRng = injectedSecureRng; // This is NOT a bug
    }

    @Override
    public String handleRequest(Map<String,String> event, Context context) {
        int randomSum = 0;
        randomSum += staticRng.nextInt();
        randomSum += staticSecureRng.nextInt();
        randomSum += rng.nextInt();
        randomSum += secureRng.nextInt();
        randomSum += injectedRng.nextInt();
        randomSum += injectedSecureRng.nextInt();
        return Integer.toString(randomSum);
    }
}
