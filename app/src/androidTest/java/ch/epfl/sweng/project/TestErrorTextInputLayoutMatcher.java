package ch.epfl.sweng.project;

import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Class in which we create a Matcher.
 */
final class TestErrorTextInputLayoutMatcher {

    /**
     * Define and return a matcher that matches TextInputLayout to
     * test the error messages displayed by a TextInputLayout.
     *
     * @param stringMatcher Matcher of String with text to match.
     * @return matcher that matches TextInputLayout.
     */
    static Matcher<View> withErrorText(final Matcher<String> stringMatcher) {

        //Return a BoundedMatcher with :
        //View : desired type for the matcher
        //TextInputLayout : subtype of View that the matcher applies safely to.m
        return new BoundedMatcher<View, TextInputLayout>(TextInputLayout.class) {

            @Override
            public void describeTo(final Description description) {
                description.appendText("with error text: ");
                stringMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(final TextInputLayout textInputLayout) {
                //The first clause checks that the CharSequence returned by getError()
                //is not null to avoid the toString() call to throw an NullPointerException.
                //The second clause checks that the error message displayed by the
                //TextInputLayout matches to the stringMatcher
                return textInputLayout.getError() != null
                        && stringMatcher.matches(textInputLayout.getError().toString());
            }
        };
    }
}

