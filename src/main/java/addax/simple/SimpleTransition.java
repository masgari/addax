package addax.simple;

import addax.Context;
import addax.Transition;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author Mamad
 * @version 1.0
 * @since 29/03/13
 */
public class SimpleTransition implements Transition<String>, Serializable {
    private static final long serialVersionUID = 1l;

    private static final Function<String, String> TO_LOWER = new Function<String, String>() {
        @Nullable
        @Override
        public String apply(@Nullable String input) {
            return input != null ? input.toLowerCase() : null;
        }
    };
    protected SimpleAction action;
    protected Pattern pattern;

    @Override
    public boolean accept(String word, Context<String> context) {
        if (isAllowed(word)) {
            if (action != null) {
                action.execute(word, context);
            }
            return true;
        }
        return false;
    }

    public SimpleTransition(String regEx, boolean ignoreCase) {
        this(Pattern.compile(regEx, ignoreCase ? Pattern.CASE_INSENSITIVE : 0));
    }

    public SimpleTransition(Pattern pattern) {
        this.pattern = pattern;
    }

    public SimpleTransition action(SimpleAction action) {
        this.action = action;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof SimpleTransition)) return false;

        SimpleTransition that = (SimpleTransition) o;
        return pattern.toString().equals(that.pattern.toString());
    }

    @Override
    public int hashCode() {
        return pattern.toString().hashCode();
    }

    @Override
    public String toString() {
        return String.format("(%s)", pattern);
    }

    public static SimpleTransition except(String... words) {
        return new ExceptTransition(words);
    }

    public static SimpleTransition anyOf(String... words) {
        return anyOf(Sets.newHashSet(words));
    }

    public static SimpleTransition anyOf(Collection<String> words) {
        return new SimpleTransition(makeRegEx(words, true), true);
    }

    protected static String makeRegEx(Collection<String> words, boolean ignoreCase) {
        return Joiner.on('|').join(ignoreCase ? Iterators.transform(words.iterator(), TO_LOWER) : words.iterator());
    }

    protected boolean isAllowed(String word) {
        return pattern.matcher(word).find();
    }

    public static SimpleTransition any() {
        return new AnyTransition();
    }

    private static class ExceptTransition extends SimpleTransition {
        private static final long serialVersionUID = 1l;

        public ExceptTransition(String... words) {
            super(SimpleTransition.makeRegEx(Arrays.asList(words), true), true);
        }

        @Override
        protected boolean isAllowed(String word) {
            return !super.isAllowed(word);
        }

        @Override
        public String toString() {
            return String.format("except(%s)", pattern.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof ExceptTransition)) return false;

            SimpleTransition that = (SimpleTransition) o;
            return pattern.toString().equals(that.pattern.toString());
        }

        @Override
        public int hashCode() {
            return super.hashCode() * 31;
        }
    }

    private static class AnyTransition extends SimpleTransition {
        private static final long serialVersionUID = 1l;

        public AnyTransition() {
            super(null);
        }

        @Override
        protected boolean isAllowed(String word) {
            return true;
        }

        @Override
        public String toString() {
            return "any";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof AnyTransition)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return 104729;//prime number from http://primes.utm.edu/lists/small/10000.txt
        }
    }

}
