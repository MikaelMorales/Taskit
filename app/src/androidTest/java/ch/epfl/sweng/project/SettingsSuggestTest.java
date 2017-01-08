package ch.epfl.sweng.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sweng.project.data.UserProvider;
import ch.epfl.sweng.project.settings.SettingsSuggestActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.uiautomator.UiDevice.getInstance;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.containsString;

public class SettingsSuggestTest {

    @BeforeClass
    public static void setUserProvider() {
        UserProvider.setProvider(Utils.TEST_PROVIDER);
    }

    @Rule
    public ActivityTestRule<SettingsSuggestActivity> mActivityRule = new ActivityTestRule<SettingsSuggestActivity>(
            SettingsSuggestActivity.class){
        //Override to be able to change the SharedPreferences effectively
        @Override
        protected void beforeActivityLaunched() {
            Context actualContext = InstrumentationRegistry.getTargetContext();
            SharedPreferences prefs = actualContext.getSharedPreferences(actualContext.getString(R.string.application_prefs_name), Context.MODE_PRIVATE);
            prefs.edit().putBoolean(actualContext.getString(R.string.first_launch), false).apply();
            prefs.edit().putBoolean(actualContext.getString(R.string.new_user), false).apply();
            super.beforeActivityLaunched();
        }
    };

    @Test
    public void CheckIfNotFilledNameErrorDisplayed() {
      SuperTest.waitForActivity();
        onView(withId(R.id.settings_suggest_button)).perform(click());

        UiDevice mUiDevice = getInstance(getInstrumentation());
        
        UiObject settingsButton = mUiDevice.findObject(new UiSelector().text("SEND"));
        try{
            settingsButton.click();
        }catch( UiObjectNotFoundException u){
            fail("something went wrong with UiAutomator action : "+u.getMessage());
        }

        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.settings_suggest_field_mandatory)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.settings_suggest_textinputlayout_name))
                .check(matches(TestErrorTextInputLayoutMatcher
                        .withErrorText(containsString(errorMessage))));
    }

    @Test
    public void CheckIfNotFilledEmailErrorDisplayed() {
        SuperTest.waitForActivity();
        String name = "Paco Lopez";

        onView(withId(R.id.settings_suggest_name)).perform(typeText(name));
        //onView(withId(R.id.settings_suggest_button)).perform(click());
        UiDevice mUiDevice = getInstance(getInstrumentation());

        mUiDevice.pressBack();

        UiObject settingsButton = mUiDevice.findObject(new UiSelector().text("SEND"));
        try{
            settingsButton.click();
        }catch( UiObjectNotFoundException u) {
            fail("something went wrong with UiAutomator action : " + u.getMessage());
        }

        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.settings_suggest_field_mandatory)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.settings_suggest_textinputlayout_email))
                .check(matches(TestErrorTextInputLayoutMatcher
                        .withErrorText(containsString(errorMessage))));
    }

    @Test
    public void CheckIfNotValidEmailErrorDisplay() {
        SuperTest.waitForActivity();
        String name = "Paco Lopez";
        String falseEmail = "false";

        onView(withId(R.id.settings_suggest_name)).perform(typeText(name));
        onView(withId(R.id.settings_suggest_mail)).perform(typeText(falseEmail));
        //onView(withId(R.id.settings_suggest_button)).perform(click());
        UiDevice mUiDevice = getInstance(getInstrumentation());

        mUiDevice.pressBack();

        UiObject settingsButton = mUiDevice.findObject(new UiSelector().text("SEND"));
        try{
            settingsButton.click();
        }catch( UiObjectNotFoundException u){
            fail("something went wrong with UiAutomator action : "+u.getMessage());
        }


        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.settings_suggest_not_valid_email)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.settings_suggest_textinputlayout_email))
                .check(matches(TestErrorTextInputLayoutMatcher
                        .withErrorText(containsString(errorMessage))));
    }

    @Test
    public void CheckIfNotFilledMessageErrorDisplay() {
        SuperTest.waitForActivity();
        String name = "Paco Lopez";
        String rightEmail = "bonjour@sion.ch";

        onView(withId(R.id.settings_suggest_name)).perform(typeText(name));
        onView(withId(R.id.settings_suggest_mail)).perform(typeText(rightEmail));
        //onView(withId(R.id.settings_suggest_button)).perform(click());

        UiDevice mUiDevice = getInstance(getInstrumentation());

        mUiDevice.pressBack();

        UiObject settingsButton = mUiDevice.findObject(new UiSelector().text("SEND"));
        try{
            settingsButton.click();
        }catch( UiObjectNotFoundException u){
            fail("something went wrong with UiAutomator action : "+u.getMessage());
        }

        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.settings_suggest_field_mandatory)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.settings_suggest_textinputlayout_message))
                .check(matches(TestErrorTextInputLayoutMatcher
                        .withErrorText(containsString(errorMessage))));
    }
 }
