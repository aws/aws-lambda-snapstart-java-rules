// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import static java.lang.System.out;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.security.SecureRandom;
import java.util.Random;
import org.crac.Resource;

public class LambdaWithCracUsingRng implements RequestHandler<String, String>, Resource {

    private final Random constructorNewRng_bad;
    private final Random constructorDirectRefRng_bad;
    private final Random constructorMethodRefRng_bad;
    private final SecureRandom constructorNewSecureRng_ok;
    private final SecureRandom constructorDirectRefSecureRng_ok;
    private final SecureRandom constructorMethodRefSecureRng_ok;
    private final Random constructorRngPolymorphedFromSecureRng_ok;
    private final Random constructorRngUpCastedFromSecureRng_ok;

    private Random checkpointNewRng_bad;
    private Random checkpointNewSecureRng_ok;
    private Random restoreNewRng_ok;
    private Random invokeNewRng_ok;

    public LambdaWithCracUsingRng(final Random rng, final SecureRandom secureRng) {
        this.constructorNewRng_bad = new Random(); // This is a bug
        this.constructorDirectRefRng_bad = rng; // This is a bug
        this.constructorMethodRefRng_bad = Random.class.cast(secureRng); // This is a bug as we assume any method returning `Random` type is potentially unsecure
        this.constructorNewSecureRng_ok = new SecureRandom();
        this.constructorDirectRefSecureRng_ok = secureRng;
        this.constructorMethodRefSecureRng_ok = SecureRandom.class.cast(secureRng);
        this.constructorRngPolymorphedFromSecureRng_ok = secureRng;
        this.constructorRngUpCastedFromSecureRng_ok = (Random) secureRng;
    }

    @Override
    public void beforeCheckpoint(org.crac.Context<? extends Resource> context) throws Exception {
        this.checkpointNewRng_bad = new Random(); // This is a bug
        this.checkpointNewSecureRng_ok = new SecureRandom();
    }

    @Override
    public void afterRestore(org.crac.Context<? extends Resource> context) throws Exception {
        this.restoreNewRng_ok = new Random();
    }

    @Override
    public String handleRequest(String event, Context context) {
        this.invokeNewRng_ok = new Random();

        out.println(constructorNewRng_bad.nextInt());
        out.println(constructorDirectRefRng_bad.nextInt());
        out.println(constructorMethodRefRng_bad.nextInt());
        out.println(constructorNewSecureRng_ok.nextInt());
        out.println(constructorDirectRefSecureRng_ok.nextInt());
        out.println(constructorMethodRefSecureRng_ok.nextInt());
        out.println(constructorRngPolymorphedFromSecureRng_ok.nextInt());
        out.println(constructorRngUpCastedFromSecureRng_ok.nextInt());
        out.println(checkpointNewRng_bad.nextInt());
        out.println(checkpointNewSecureRng_ok.nextInt());
        out.println(restoreNewRng_ok.nextInt());
        out.println(invokeNewRng_ok.nextInt());

        return "200";
    }

}
