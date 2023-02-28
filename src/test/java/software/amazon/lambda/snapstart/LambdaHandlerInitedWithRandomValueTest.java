// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.lambda.snapstart;

import static edu.umd.cs.findbugs.test.CountMatcher.containsExactly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static software.amazon.lambda.snapstart.matcher.ContainsMatcher.containsAll;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SystemProperties;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcher;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcherBuilder;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class LambdaHandlerInitedWithRandomValueTest extends AbstractSnapStartTest {

    @Test
    public void testGoodCase() {
        BugCollection bugCollection = findBugsInLambda("GoodLambda");
        assertThat(bugCollection, containsExactly(0, snapStartBugMatcher().build()));
    }

    @Test
    public void testNonLambdaCase() {
        BugCollection bugCollection = findBugsInLambda("NonLambda");
        assertThat(bugCollection, containsExactly(0, snapStartBugMatcher().build()));
    }

    @Test
    public void testBadCase() {
        SystemProperties.setProperty("findbugs.execplan.debug", "true");
        BugCollection bugCollection = findBugsInLambda("BadLambda");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("BadLambda");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                bugMatcherBuilder.atField("RNG").atLine(19).build(),
                bugMatcherBuilder.atField("STATIC_USER_ID_DOUBLE").atLine(24).build(),
                bugMatcherBuilder.atField("STATIC_USER_ID_INT").atLine(25).build(),
                bugMatcherBuilder.atField("STATIC_USER_ID_INT_SEC").atLine(26).build(),
                bugMatcherBuilder.atField("STATIC_USER_ID_UUID").atLine(23).build(),
                bugMatcherBuilder.atField("userIdDouble").atLine(31).build(),
                bugMatcherBuilder.atField("userIdUuid").atLine(27).build()
        );

        assertThat(bugCollection, containsAll(expectedBugs));
    }

    @Test
    public void rngMemberFieldBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingRandom");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("LambdaUsingRandom");

        assertThat(bugCollection, containsExactly(1, bugMatcherBuilder.atField("staticRng").atLine(14).build()));
        assertThat(bugCollection, containsExactly(1, bugMatcherBuilder.atField("rng").atLine(22).build()));
        assertThat(bugCollection, containsExactly(1, bugMatcherBuilder.atField("injectedRng").atLine(24).build()));
        assertThat(bugCollection, containsExactly(3, snapStartBugMatcher().build()));
    }

    @Test
    public void transitiveRngInstancesBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingTransitiveRng");

        BugInstanceMatcher bugMatcher = snapStartBugMatcher().build();
        toBeFixed(() -> assertThat(bugCollection, not(containsExactly(0, bugMatcher))));
    }

    @Test
    public void customRngMemberFieldBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingRngLib", "RngLib", "DeepRngLib");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("LambdaUsingRngLib");

        List<Matcher<BugInstance>> expectedUncaughtBugs = Arrays.asList(
                bugMatcherBuilder.atField("staticRngLib").atLine(12).build(),
                bugMatcherBuilder.atField("rngLib").atLine(16).build()
        );

        toBeFixed(() -> assertThat(bugCollection, containsAll(expectedUncaughtBugs)));

        List<Matcher<BugInstance>> expectedCaughtBugs = Arrays.asList(
                bugMatcherBuilder.atField("randomLogId").atLine(21).build(),
                bugMatcherBuilder.atField("randomUserId").atLine(20).build()
        );

        assertThat(bugCollection, containsAll(expectedCaughtBugs));
    }

    @Test
    public void uuidMemberFieldBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingUuid");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("LambdaUsingUuid");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                bugMatcherBuilder.atField("ID1").atLine(13).build(),
                bugMatcherBuilder.atField("ID2").atLine(14).build(),
                bugMatcherBuilder.atField("id3").atLine(22).build(),
                bugMatcherBuilder.atField("id4").atLine(16).build(),
                bugMatcherBuilder.atField("id5").atLine(22).build()
        );

        toBeFixed(() -> assertThat(bugCollection, containsAll(expectedBugs)));
    }

    @Test
    public void timestampMemberFieldBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingTs");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("LambdaUsingTs");

        assertThat(bugCollection, containsExactly(1, bugMatcherBuilder.atField("tsFromSystemTimeMillis").atLine(24).build()));
        assertThat(bugCollection, containsExactly(1, bugMatcherBuilder.atField("tsFromSystemTimeNano").atLine(25).build()));
        assertThat(bugCollection, containsExactly(1, bugMatcherBuilder.atField("tsFromInstantNow").atLine(26).build()));
        assertThat(bugCollection, containsExactly(1, bugMatcherBuilder.atField("tsFromClock").atLine(27).build()));
        toBeFixed(() -> assertThat(bugCollection, containsExactly(1, bugMatcherBuilder.atField("logName").atLine(29).build())));
        assertThat(bugCollection, containsExactly(4, snapStartBugMatcher().build()));
        toBeFixed(() -> assertThat(bugCollection, containsExactly(5, snapStartBugMatcher().build())));
    }

    @Test
    public void findsBugsInClassesThatLookLikeLambda() {
        BugCollection bugCollection = findBugsInClasses("LooksLikeLambda", "LooksLikeStreamLambda");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                snapStartBugMatcher().inClass("LooksLikeLambda").atField("LOG_ID").atLine(11).build(),
                snapStartBugMatcher().inClass("LooksLikeStreamLambda").atField("LOG_ID").atLine(13).build()
        );

        assertThat(bugCollection, containsAll(expectedBugs));
    }

    @Test
    public void findsBugsInClassesThatImplementCracResource() {
        String className = "LambdaWithCracUsingRng";
        BugCollection bugCollection = findBugsInClasses(className);

        assertThat(bugCollection, containsExactly(1, snapStartBugMatcher().inClass(className).atField("constructorNewRng_bad").atLine(31).build()));
        assertThat(bugCollection, containsExactly(1, snapStartBugMatcher().inClass(className).atField("constructorDirectRefRng_bad").atLine(32).build()));
        assertThat(bugCollection, containsExactly(1, snapStartBugMatcher().inClass(className).atField("constructorMethodRefRng_bad").atLine(33).build()));
        assertThat(bugCollection, containsExactly(1, snapStartBugMatcher().inClass(className).atField("checkpointNewRng_bad").atLine(43).build()));
        assertThat(bugCollection, containsExactly(4, snapStartBugMatcher().inClass(className).build()));
    }

    @Test
    public void testLambdaWithToString() {
        BugCollection bugCollection = findBugsInClasses("LambdaWithToString");
        assertThat(bugCollection, containsExactly(1, snapStartBugMatcher().inClass("LambdaWithToString").atField("random").atLine(11).build()));
    }

    @Test
    public void testClassImplementingFunctionalInterface() {
        BugCollection bugCollection = findBugsInClasses("ImplementsFunctionalInterface");
        assertThat(bugCollection, containsExactly(1, snapStartBugMatcher().inClass("ImplementsFunctionalInterface").atField("random").atLine(8).build()));
    }

    @Test
    public void testLambdaWithNoInterface() {
        BugCollection bugCollection = findBugsInClasses("LambdaHandlerWithNoInterface");
        assertThat(bugCollection, containsExactly(1, snapStartBugMatcher().inClass("LambdaHandlerWithNoInterface").atField("random").atLine(7).build()));
    }
  
    @Test
    public void testLambdaWithDependencyInjection() {
        BugCollection bugCollection = findBugsInClasses("DependencyInjection", "MyDependency");
        assertThat(bugCollection, containsExactly(1, snapStartBugMatcher().inClass("MyDependency").atField("random").atLine(6).build()));
    }

    // TODO fix all tests using this and remove this method eventually!
    private static void toBeFixed(Executable e) {
        assertThrows(AssertionError.class, e);
    }
}
