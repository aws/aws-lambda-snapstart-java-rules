package software.amazon.lambda.snapstart;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.test.AnalysisRunner;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcherBuilder;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractSnapStartTest {

    /**
     * All example classes that are subject to our tests should be under this package.
     */
    private static final String EXAMPLE_PKG = "software.amazon.lambda.snapstart.lambdaexamples".replace('.', '/');

    private AnalysisRunner runner;

    @BeforeEach
    public void init() {
        runner = new AnalysisRunner();
        // Our test fixture classes under lambdaexamples package use the test classpath and we
        // want analyzer to have access the bytecode of everything that fixture classes can use.
        // Therefore, the whole test classpath is added as auxiliary classpath entry here.
        String cPath = ManagementFactory.getRuntimeMXBean().getClassPath();
        for (String p : cPath.split(File.pathSeparator)) {
            runner.addAuxClasspathEntry(Paths.get(p));
        }
    }

    @AfterEach
    public void clean() {
        runner = null;
    }

    protected BugCollection findBugsInClasses(String ... classNames) {
        Path [] paths = new Path[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            paths[i] = Paths.get("target/test-classes", EXAMPLE_PKG, className + ".class");
        }
        return runner.run(paths).getBugCollection();
    }

    protected BugCollection findBugsInLambda(String className) {
        return findBugsInClasses(className);
    }

    protected BugInstanceMatcherBuilder snapStartBugMatcher() {
        return new BugInstanceMatcherBuilder()
                .bugType("AWS_LAMBDA_SNAP_START_BUG");
    }
}
