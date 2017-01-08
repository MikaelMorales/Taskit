package ch.epfl.sweng.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import com.facebook.FacebookSdk;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sweng.project.data.UserProvider;
import ch.epfl.sweng.project.settings.SettingsActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class SettingsTest extends SuperTest{

    @BeforeClass
    public static void setUserProvider() {
        UserProvider.setProvider(Utils.TEST_PROVIDER);
    }

    @Rule
    public ActivityTestRule<SettingsActivity> mActivityRule = new ActivityTestRule<SettingsActivity>(
            SettingsActivity.class){
        //Override to be able to change the SharedPreferences effectively
        @Override
        protected void beforeActivityLaunched(){
            Context actualContext = InstrumentationRegistry.getTargetContext();
            SharedPreferences prefs = actualContext.getSharedPreferences(actualContext.getString(R.string.application_prefs_name), Context.MODE_PRIVATE);
            prefs.edit().putBoolean(actualContext.getString(R.string.first_launch), false).apply();
            prefs.edit().putBoolean(actualContext.getString(R.string.new_user), false).apply();
            FacebookSdk.sdkInitialize(actualContext);
            super.beforeActivityLaunched();
        }
    };

    @Test
    public void CheckIfOpenTutorialFromSettings(){
        SuperTest.waitForActivity();
        onView(withId(R.id.settings_text_tutorial)).perform(click());
        onView(withId(R.id.next)).check(matches(isDisplayed()));

    }

    @Test
    public void AfterOpenTutoFromSettingsGoBackToSettings(){
        SuperTest.waitForActivity();
        onView(withId(R.id.settings_text_tutorial)).perform(click());
        onView(withId(R.id.next)).check(matches(isDisplayed()));
        onView(withId(R.id.skip)).perform(click());
        onView(withId(R.id.settings_text_tutorial)).check(matches(isDisplayed()));
    }

    @Test
    public void logoutFromSettings() {
        SuperTest.waitForActivity();
        onView(withId(R.id.settings_text_logout)).perform(click());
        onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()));
    }

    @Test
    public void CheckIfOpenAboutFromSettings() {
        SuperTest.waitForActivity();
        onView(withId(R.id.settings_text_about)).perform(click());
        onView(withId(R.id.settings_about_text_bastian)).check(matches(isDisplayed()));
    }

    @Test
    public void CheckIfOpenSuggestFromSettings() {
        SuperTest.waitForActivity();
        onView(withId(R.id.settings_text_suggest)).perform(click());
        onView(withId(R.id.settings_suggest_message)).check(matches(isDisplayed()));
    }

}
