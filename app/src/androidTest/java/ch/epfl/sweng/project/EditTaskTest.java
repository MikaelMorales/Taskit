package ch.epfl.sweng.project;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * Unit tests!
 */
@RunWith(AndroidJUnit4.class)
public final class EditTaskTest extends SuperTest {
    @Rule
    public final ExpectedException thrownException = ExpectedException.none();
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);
    private String mEditedTitle;
    private String mOldTitle;
    private String mOldDescription;

    @Before
    public void init() {
        mEditedTitle = "Edited Title";
        mOldTitle = "title number ";
        mOldDescription = "description number ";
    }

    /**
     * Test that we can't put an existing title when editing a task.
     */
    @Test
    public void testCannotEditTaskWithAlreadyExistingTitle() {

        //Create two tasks
        for (int i = 0; i < createdTask; i++) {
            createATask(mOldTitle + i, mOldDescription + i);
        }

        //Try to edit the first task to put the same title as the first task
        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Update the title with an existing one
        onView(withId(R.id.nameLinearLayout)).perform(click());
        onView(withId(R.id.title_task)).perform(click());
        onView(withId(R.id.title_task)).perform(clearText());
        onView(withId(R.id.title_task)).perform(typeText(mOldTitle + 1));
        pressBack();

        //Get the error message
        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.error_title_duplicated)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.title_task))
                .check(matches(TestErrorTextMatcher.withErrorText(containsString(errorMessage))));

        //Go back to the main activity for the next test
        pressBack();
        emptyDatabase(createdTask);
    }

    /**
     * Test that we can't edit a task and put an empty title.
     */
    @Test
    public void testCannotAddTaskWithEmptyTitle() {
        //Create a task
        createATask(mOldTitle, mOldDescription);

        //Try to edit the first task to put the same title as the first task
        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Update the title with empty string
        onView(withId(R.id.nameLinearLayout)).perform(click());
        onView(withId(R.id.title_task)).perform(click());
        onView(withId(R.id.title_task)).perform(clearText());
        pressBack();

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
        emptyDatabase(1);
    }

    /**
     *
     */
    @Test
    public void testCanEditTaskTitle() {
        //Create a task
        createATask(mOldTitle, mOldDescription);

        //Try to edit the first task to put the same title as the first task
        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Update the title and the description
        onView(withId(R.id.nameLinearLayout)).perform(click());
        onView(withId(R.id.title_task)).perform(click());
        onView(withId(R.id.title_task)).perform(clearText());
        pressBack();
        onView(withId(R.id.title_task)).perform(typeText(mEditedTitle));
        pressBack();

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withId(R.id.text_name)).check(matches(withText(mEditedTitle)));

        pressBack();
        //empty the database for the next test
        emptyDatabase(1);
    }

    @Test
    public void canDeleteATask() {
        String taskToDeleteTitle = "Task1";
        String taskToCheckTitle = "Task2";
        String taskToDeleteDescr = "Description1";
        String taskToCheckDescr = "Description2";

        //create two tasks
        createATask(taskToDeleteTitle,taskToDeleteDescr);
        createATask(taskToCheckTitle, taskToCheckDescr);

        //open information settings
        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(0))
                .perform(click());

        onView(withId(R.id.trash_menu)).perform(click());

        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(0))
                .check(matches(hasDescendant(withText(taskToCheckTitle))));

        //empty the database for the next test
        emptyDatabase(1);
    }
}