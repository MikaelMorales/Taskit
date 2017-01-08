package ch.epfl.sweng.project;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.uiautomator.UiDevice.getInstance;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

@RunWith(AndroidJUnit4.class)
public final class SortTaskTest extends SuperTest{

    private List<String> taskNames;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void addNewTask() {
        String taskQuick = "reply mail";
        String taskNormal = "withdraw cash";
        String taskLong = "homework";
        
        taskNames = Arrays.asList(taskQuick, taskNormal, taskLong);

        addTask(taskQuick, R.id.energy_low, 1);
        addTask(taskNormal, R.id.energy_normal, 2);
        addTask(taskLong, R.id.energy_high, 4);
    }

    @Test
    public void testDynamicSortWithMoreThan2Hours() {

        //Open task information
        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        pressBack();

        // select more than 2 hours at disposition
        onView(withId(R.id.time_user)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(4).perform(click());

        // check if sorted correctly
        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(0))
                .check(matches(hasDescendant(withText(taskNames.get(2)))));
        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(1))
                .check(matches(hasDescendant(withText(taskNames.get(1)))));
        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(2))
                .check(matches(hasDescendant(withText(taskNames.get(0)))));
    }

    @Test
    public void testDynamicSortWithMoreThan30Min() {

        //Open task information
        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        pressBack();

        // select more than 30 minutes at disposition
        onView(withId(R.id.time_user)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(2).perform(click());

        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(0))
                .check(matches(hasDescendant(withText(taskNames.get(1)))));
        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(1))
                .check(matches(hasDescendant(withText(taskNames.get(0)))));
        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(2))
                .check(matches(hasDescendant(withText(taskNames.get(2)))));

    }

    private void addTask(String taskName, int radioButtonId, int indexDuration) {
        UiDevice mUiDevice = getInstance(getInstrumentation());
        onView(withId(R.id.add_task_button)).perform(click());
        onView(withId(R.id.title_task)).perform(typeText(taskName));
        pressBack();

        //add a due date (today due date)
        onView(withId(R.id.pick_date)).perform(click());
        UiObject okButton = mUiDevice.findObject(new UiSelector().text("OK"));
        try{
            okButton.click();
        }catch(UiObjectNotFoundException u){
            fail("Could not confirm date selection "+u.getMessage());
        }

        onView(withId(radioButtonId)).perform(click());


        // select the duration
        onView(withId(R.id.durationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(indexDuration).perform(click());

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());
    }
}
