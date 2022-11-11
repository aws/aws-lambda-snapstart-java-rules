package software.amazon.lambda.snapstart.matcher;

import java.util.Arrays;
import java.util.Iterator;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeMatcher;

public final class ContainsMatcher<T> extends TypeSafeMatcher<Iterable<T>> {

    private final Iterable<Matcher<T>> matchers;
    private final Description description;

    public ContainsMatcher(Iterable<Matcher<T>> matchers) {
        this.matchers = matchers;
        this.description =  new StringDescription();
    }

    public static <T> Matcher<Iterable<T>> containsAll(final Iterable<Matcher<T>> matchers) {
        return new ContainsMatcher<>(matchers);
    }

    public static <T> Matcher<Iterable<T>> containsAll(final Matcher<T> ... matchers) {
        return containsAll(Arrays.asList(matchers));
    }

    @Override
    protected boolean matchesSafely(Iterable<T> items) {

        Iterator<T> itemsIterator = items.iterator();
        Iterator<Matcher<T>> matcherIterator = matchers.iterator();

        boolean allItemsMatched = true;
        while (matcherIterator.hasNext()) {
            Matcher<T> matcher = matcherIterator.next();

            T item = null;
            if (itemsIterator.hasNext()) {
                item = itemsIterator.next();
            }

            if (!matcher.matches(item)) {
                matcher.describeTo(description);
                description.appendText("\n");
                allItemsMatched = false;
            }
        }
        return allItemsMatched;
    }

    @Override
    public void describeTo(final Description desc) {
        desc.appendText(description.toString());
    }
}

