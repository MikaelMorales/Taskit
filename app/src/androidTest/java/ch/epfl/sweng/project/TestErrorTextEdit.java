package ch.epfl.sweng.project;

import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

final class TestErrorTextEdit {
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
            public boolean matchesSafely(final TextInputLayout textInputLayout) {
                return textInputLayout.getError() != null
                        && stringMatcher.matches(textInputLayout.getError().toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with error text: ");
                stringMatcher.describeTo(description);
            }
        };
    }
}
