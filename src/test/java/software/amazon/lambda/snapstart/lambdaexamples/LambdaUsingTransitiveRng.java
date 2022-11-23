// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import org.crac.Resource;

public class LambdaUsingTransitiveRng implements RequestHandler<Map<String,String>, String>, Resource  {

    /**
     * Library class that does not implement {@link RequestHandler} or {@link Resource},
     * but is used by {@link LambdaUsingTransitiveRng} which is a Lambda handler.
     *
     * Note: For the purpose of the detector, it should not matter whether this is an inner class
     * or a separate class, so as long as this class is provided in the classpath of SpotBugs execution.
     */
    public static class LogicLib {
        final static Random staticRng = new Random();
        final static SecureRandom staticSecureRng = new SecureRandom();

        final Random rng;
        final SecureRandom secureRng;

        // RNGs set in class outside of Lambda handler
        public LogicLib(Random rng, SecureRandom secureRng) {
            this.rng = rng;
            this.secureRng = secureRng;
        }

        public int getRngValue() {
            return rng.nextInt();
        }
    }

    private static final LogicLib staticLogicLib = new LogicLib(new Random(), new SecureRandom());

    private final LogicLib constructorLogicLib;
    private LogicLib checkpointLogicLib;
    private LogicLib restoreLogicLib;

    public LambdaUsingTransitiveRng(Random rng, SecureRandom secureRng) {
        this.constructorLogicLib = new LogicLib(rng, secureRng);
    }

    @Override // for org.crac.Resource
    public void beforeCheckpoint(org.crac.Context<? extends Resource> context) throws Exception {
        this.checkpointLogicLib = new LogicLib(new Random(), new SecureRandom());

    }

    @Override // for org.crac.Resource
    public void afterRestore(org.crac.Context<? extends Resource> context) throws Exception {
        this.restoreLogicLib = new LogicLib(new Random(), new SecureRandom());
    }

    @Override // for RequestHandler
    public String handleRequest(Map<String,String> event, Context context) {
        int sum = 0;

        sum += LogicLib.staticRng.nextInt(); // This should be a bug
        sum += LogicLib.staticSecureRng.nextInt();

        sum += getBadRngValue();
        sum += getOkayRngValue();

        sum += staticLogicLib.getRngValue(); // This should be a bug
        sum += constructorLogicLib.getRngValue(); // This should be a bug
        sum += checkpointLogicLib.getRngValue(); // This should be a bug
        sum += restoreLogicLib.getRngValue();

        return Integer.toString(sum);
    }

    private int getBadRngValue() {
        int sum = 0;
        sum += staticLogicLib.rng.nextInt(); // This should be a bug
        sum += constructorLogicLib.rng.nextInt(); // This should be a bug
        sum += checkpointLogicLib.rng.nextInt(); // This should be a bug
        return sum;
    }

    private int getOkayRngValue() {
        int sum = 0;
        sum += staticLogicLib.secureRng.nextInt();
        sum += constructorLogicLib.secureRng.nextInt();
        sum += checkpointLogicLib.secureRng.nextInt();
        sum += restoreLogicLib.rng.nextInt();
        sum += restoreLogicLib.secureRng.nextInt();
        return sum;
    }

}
