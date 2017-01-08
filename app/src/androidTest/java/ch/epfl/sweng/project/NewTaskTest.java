package ch.epfl.sweng.project;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.uiautomator.UiDevice.getInstance;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


/**
 * Unit tests!
 */
@RunWith(AndroidJUnit4.class)
public final class NewTaskTest extends SuperTest {
    @Rule
    public final ExpectedException thrownException = ExpectedException.none();
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);
    private String mTitleToBeTyped;
    private String mDescriptionToBeTyped;

    @Before
    public void initValidString() {
        mTitleToBeTyped = "test title number ";
        mDescriptionToBeTyped = "test description number ";
    }

    @Test
    public void packageNameIsCorrect() {
        final Context context = getTargetContext();
        assertThat(context.getPackageName(), is("ch.epfl.sweng.project"));
    }

    /**
     * Test that a Task has been correctly created and added
     * in the ListView.
     */
    @Test
    public void testCanAddTask() {
        for (int i = 0; i < createdTask; i++) {
            createATask(mTitleToBeTyped + i, mDescriptionToBeTyped + i);
            //Check title name inside listView
            onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                    .atPosition(i))
                    .check(matches(hasDescendant(withText(mTitleToBeTyped + i))));
        }

        emptyDatabase(createdTask);
    }


    @Test
    public void futureTaskDisplayDays(){
        UiDevice mUiDevice = getInstance(getInstrumentation());

        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(typeText("task"));
        pressBack();

        //add a due date !!! Warning, test only working on 30 days month and only if they are launch before the 29
        onView(withId(R.id.pick_date)).perform(click());
        UiObject thirtyButton = mUiDevice.findObject(new UiSelector().text("30"));
        UiObject okButton = mUiDevice.findObject(new UiSelector().text("OK"));
        try{
            thirtyButton.click();
            okButton.click();
        }catch(UiObjectNotFoundException u){
            fail("Could not confirm date selection "+u.getMessage());
        }

        //add a duration
        onView(withId(R.id.durationSpinner)).perform(click());
        onData(allOf(Matchers.is(instanceOf(String.class)), Matchers.is("1 hour"))).perform(click());

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withContentDescription("represent the remaining days")).check(matches(withText(containsString("days left"))));
    }

    /**
     * Test which indicator is displayed in listView (late or due today)
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Test
    public void dueTaskDisplayDays(){
        UiDevice mUiDevice = getInstance(getInstrumentation());

        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(typeText("task"));
        pressBack();

        final Calendar c = Calendar.getInstance();
        final int currentDay = c.get(Calendar.DAY_OF_MONTH);

        onView(withId(R.id.pick_date)).perform(click());
        UiObject thirtyButton = mUiDevice.findObject(new UiSelector().text("1"));
        UiObject okButton = mUiDevice.findObject(new UiSelector().text("OK"));
        try{
            thirtyButton.click();
            okButton.click();
        }catch(UiObjectNotFoundException u){
            fail("Could not confirm date selection "+u.getMessage());
        }

        //add a duration
        onView(withId(R.id.durationSpinner)).perform(click());
        onData(allOf(Matchers.is(instanceOf(String.class)), Matchers.is("1 hour"))).perform(click());

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        if(currentDay == 1) {
            onView(withContentDescription("represent the remaining days"))
                    .check(matches(withText(containsString("today"))));
        } else {
            onView(withContentDescription("represent the remaining days"))
                    .check(matches(withText(containsString("late"))));
        }
    }

    @Test
    public void testCanDeleteTasks() {
        //We create and add tasks
        for (int i = 0; i < createdTask; i++) {
            createATask(mTitleToBeTyped + i, mDescriptionToBeTyped + i);
        }

        //We delete the tasks
        for (int i = 0; i < createdTask; i++) {
            onView(withId(R.id.list_view_tasks))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeLeft()));

            //Test if the tasks are correctly deleted
            if (i != createdTask-1) {
                onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                        .atPosition(0))
                        .check(matches(hasDescendant(withText(mTitleToBeTyped + (i+1)))));
            }
        }
    }

    @Test
    public void testCanDoneTasks() {
        //We create and add tasks
        for (int i = 0; i < createdTask; i++) {
            createATask(mTitleToBeTyped + i, mDescriptionToBeTyped + i);
        }

        //We mark done the task
        for (int i = 0; i < createdTask; i++) {
            onView(withId(R.id.list_view_tasks))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeRight()));

            //Test if the tasks are correctly removed
            if (i != createdTask-1) {
                onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                        .atPosition(0))
                        .check(matches(hasDescendant(withText(mTitleToBeTyped + (i+1)))));
            }
        }
    }


    /**
     * Test that we can't add a task with an empty title.
     */
    @Test
    public void testCannotAddTaskWithEmptyTitle() {
        //Create a task with empty titles
        onView(withId(R.id.add_task_button)).perform(click());
        onView(withId(R.id.title_task)).perform(click());
        onView(withId(R.id.title_task)).perform(typeText(""));
        pressBack();
        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        //Get the error message
        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.error_title_empty)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.title_task))
                .check(matches(TestErrorTextMatcher.withErrorText(containsString(errorMessage))));
        pressBack();
    }

    /**
     * Test that we can't add a task with an already used title.
     */
    @Test
    public void testCannotAddTaskWithExistingTitle() {
        //Create a first task
        createATask(mTitleToBeTyped, mDescriptionToBeTyped);

        //Try to create a second class with the same title as the first one
        onView(withId(R.id.add_task_button)).perform(click());
        onView(withId(R.id.title_task)).perform(typeText(mTitleToBeTyped));
        pressBack();
        //Check that the done editing button is not displayed
        onView(withId(R.id.edit_done_button_toolbar)).check(matches(not(isDisplayed())));


        //Get the error message
        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.error_title_duplicated)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.title_task))
                .check(matches(TestErrorTextMatcher.withErrorText(containsString(errorMessage))));
        pressBack();
        emptyDatabase(1);
    }
}
