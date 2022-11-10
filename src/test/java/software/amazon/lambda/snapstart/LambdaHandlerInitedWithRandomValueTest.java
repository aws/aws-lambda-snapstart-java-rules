package software.amazon.lambda.snapstart;

import static edu.umd.cs.findbugs.test.CountMatcher.containsExactly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static software.amazon.lambda.snapstart.matcher.ContainsMatcher.containsAll;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SystemProperties;
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
        BugInstanceMatcherBuilder badLambdaBugBuilder = snapStartBugMatcher().inClass("BadLambda");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                badLambdaBugBuilder.atField("STATIC_USER_ID_DOUBLE").atLine(21).build(),
                badLambdaBugBuilder.atField("STATIC_USER_ID_INT").atLine(22).build(),
                badLambdaBugBuilder.atField("STATIC_USER_ID_INT_SEC").atLine(23).build(),
                badLambdaBugBuilder.atField("STATIC_USER_ID_UUID").atLine(20).build(),
                badLambdaBugBuilder.atField("userIdDouble").atLine(28).build(),
                badLambdaBugBuilder.atField("userIdUuid").atLine(24).build()
        );

        assertThat(bugCollection, containsAll(expectedBugs));
    }

    @Test
    public void rngMemberFieldBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingRandom");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("LambdaUsingRandom");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                bugMatcherBuilder.atField("staticRng").atLine(10).build(),
                bugMatcherBuilder.atField("rng").atLine(14).build()
        );

        toBeFixed(() -> assertThat(bugCollection, containsAll(expectedBugs)));
    }

    @Test
    public void customRngMemberFieldBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingRngLib", "RngLib");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("LambdaUsingRngLib");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                bugMatcherBuilder.atField("staticRngLib").atLine(9).build(),
                bugMatcherBuilder.atField("rngLib").atLine(13).build()
        );

        toBeFixed(() -> assertThat(bugCollection, containsAll(expectedBugs)));
    }

    @Test
    public void uuidMemberFieldBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingUuid");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("LambdaUsingUuid");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                bugMatcherBuilder.atField("ID1").atLine(10).build(),
                bugMatcherBuilder.atField("ID2").atLine(11).build(),
                bugMatcherBuilder.atField("id3").atLine(19).build(),
                bugMatcherBuilder.atField("id4").atLine(13).build(),
                bugMatcherBuilder.atField("id5").atLine(19).build()
        );

        toBeFixed(() -> assertThat(bugCollection, containsAll(expectedBugs)));
    }

    @Test
    public void timestampMemberFieldBreaksSnapStart() {
        BugCollection bugCollection = findBugsInClasses("LambdaUsingTs");
        BugInstanceMatcherBuilder bugMatcherBuilder = snapStartBugMatcher().inClass("LambdaUsingTs");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                bugMatcherBuilder.atField("logName").atLine(13).build()
        );

        toBeFixed(() -> assertThat(bugCollection, containsAll(expectedBugs)));
    }

    @Test
    public void findsBugsInClassesThatLookLikeLambda() {
        BugCollection bugCollection = findBugsInClasses("LooksLikeLambda", "LooksLikeStreamLambda");

        List<Matcher<BugInstance>> expectedBugs = Arrays.asList(
                snapStartBugMatcher().inClass("LooksLikeLambda").atField("LOG_ID").atLine(8).build(),
                snapStartBugMatcher().inClass("LooksLikeStreamLambda").atField("LOG_ID").atLine(10).build()
        );

        assertThat(bugCollection, containsAll(expectedBugs));
    }

    // TODO fix all tests using this and remove this method eventually!
    private static void toBeFixed(Executable e) {
        assertThrows(AssertionError.class, e);
    }
}
