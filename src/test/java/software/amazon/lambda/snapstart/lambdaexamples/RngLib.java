// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart.lambdaexamples;

import java.util.Random;

public class RngLib {
    private final Random random;

    public RngLib() {
        this.random = new Random(0);
    }

    public int randomInt() {
        return random.nextInt();
    }
}
