package ch.epfl.sweng.project;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.EditText;

import org.hamcrest.Description;

final class TestErrorTextMatcher {
    /**
     * Define and return a matcher that matches EditText to
     * test the error messages displayed by a EditText.
     *
     * @param stringMatcher Matcher of String with text to match.
     * @return matcher that matches EditText.
     */
    static org.hamcrest.Matcher<View> withErrorText(final org.hamcrest.Matcher<String> stringMatcher) {

        //Return a BoundedMatcher with :
        //View : desired type for the matcher
        //EditText : subtype of View that the matcher applies safely to.m
        return new BoundedMatcher<View, EditText>(EditText.class) {

            @Override
            public void describeTo(final Description description) {
                description.appendText("with error text: ");
                stringMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(final EditText editText) {
                //The first clause checks that the CharSequence returned by getError()
                //is not null to avoid the toString() call to throw an NullPointerException.
                //The second clause checks that the error message displayed by the
                //EditText matches to the stringMatcher
                return editText.getError() != null
                        && stringMatcher.matches(editText.getError().toString());
            }
        };
    }
}
