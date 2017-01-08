package ch.epfl.sweng.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sweng.project.data.UserProvider;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class EntryActivityIntroTest {

    @BeforeClass
    public static void setUserProvider() {
        UserProvider.setProvider(Utils.TEST_PROVIDER);
    }

    @Rule
    public ActivityTestRule<EntryActivity> mActivityRule = new ActivityTestRule<EntryActivity>(
            EntryActivity.class){
        //Override to be able to change the SharedPreferences effectively
        @Override
        protected void beforeActivityLaunched(){
            Context actualContext = InstrumentationRegistry.getTargetContext();
            SharedPreferences prefs = actualContext.getSharedPreferences(actualContext.getString(R.string.application_prefs_name), Context.MODE_PRIVATE);
            prefs.edit().putBoolean(actualContext.getString(R.string.first_launch), true).apply();
            prefs.edit().putBoolean(actualContext.getString(R.string.new_user), true).apply();
            super.beforeActivityLaunched();
        }
    };

    @Test
    public void inFirstLaunchCaseLaunchIntroActivity(){
        SuperTest.waitForActivity();
        onView(withId(R.id.next)).check(matches(isDisplayed()));
    }

    @Test
    public void inFirstLaunchCaseIfSkipTutoGoToEntryActivity(){
        SuperTest.waitForActivity();
        onView(withId(R.id.skip)).perform(click());
        onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()));
    }
}
