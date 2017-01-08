package ch.epfl.sweng.project;


import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.uiautomator.UiDevice.getInstance;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class ContributorsTest extends SuperTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void sharedTasIsDisplayedTest(){
        String taskTitle = "test";

        UiDevice mUiDevice = getInstance(getInstrumentation());

        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(typeText(taskTitle));
        pressBack();

        //add a due date (today due date)
        onView(withId(R.id.pick_date)).perform(click());
        UiObject okButton = mUiDevice.findObject(new UiSelector().text("OK"));
        try{
            okButton.click();
        }catch(UiObjectNotFoundException u){
            fail("Could not confirm date selection "+u.getMessage());
        }

        //add a duration
        onView(withId(R.id.durationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("1 hour"))).perform(click());


        UiScrollable scrollDown = new UiScrollable(new UiSelector().scrollable(true));
        try{
            scrollDown.scrollForward();
            onView(withId(R.id.addContributorButton)).perform(click());
            UiObject contributorText = mUiDevice.findObject(new UiSelector().className("android.widget.EditText"));
            contributorText.setText("cedric.viaccoz@gmail.com");
            UiObject OKWidget = mUiDevice.findObject(new UiSelector().text("OK"));
            OKWidget.click();
            onView(withId(R.id.edit_done_button_toolbar)).perform(click());
        }catch(UiObjectNotFoundException u){
            fail("Something went wrong with UiAutomator actions "+u.getMessage());
        }
        onView(withId(R.id.imageSharedTask)).check(matches(isDisplayed()));
    }
}
