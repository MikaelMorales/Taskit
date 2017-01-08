package ch.epfl.sweng.project;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.project.data.TaskProvider;
import ch.epfl.sweng.project.data.UserProvider;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.uiautomator.UiDevice.getInstance;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class UnfilledTasksTest {

    private UiDevice mUiDevice;
    private static final String LEFT_ARROW_CLASSN = "android.widget.ImageButton";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @BeforeClass
    public static void setTaskProvider() {
        TaskProvider.setProvider(Utils.TEST_PROVIDER);
    }

    @BeforeClass
    public static void setUserProvider() {
        UserProvider.setProvider(Utils.TEST_PROVIDER);
    }

    @Before
    public void setup(){
        mUiDevice = getInstance(getInstrumentation());
    }


    @Test
    public void MainActivityToUnfilledRoundTrip(){
        onView(withId(R.id.add_task_button)).perform(click());

        //add title only to get an unfilled task
        onView(withId(R.id.title_task)).perform(typeText("unfTask"));
        pressBack();
        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withId(R.id.unfilled_task_button)).perform(click());
        onView(withId(R.id.unfilled_tasks_toolbar)).check(matches(isDisplayed()));

        UiObject returnButton = mUiDevice.findObject(new UiSelector().className(LEFT_ARROW_CLASSN).index(0));
        try{
            returnButton.clickAndWaitForNewWindow();
        }catch(UiObjectNotFoundException u){
            fail("Could not find the return arrow with UiAutomator actions");
        }

        onView(withId(R.id.add_task_button)).check(matches(isDisplayed()));
    }

    @Test
    public void createATaskWithoutDueDateTriggersTableRowDisplay(){
        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(typeText("unfTask"));
        pressBack();

        //add a duration
        onView(withId(R.id.durationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("1 hour"))).perform(click());

        //TODO: When the issue of the location spinner during the test is solved, uncomment this.
        //add a location
        /*onView(withId(R.id.locationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Everywhere"))).perform(click());
        */
        onView(withId(R.id.edit_done_button_toolbar)).perform(click());
    }


    @Test
    public void createATaskWithoutDurationTriggersTableRowDisplay(){
        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(click());
        onView(withId(R.id.title_task)).perform(typeText("unfTask"));
        pressBack();

        //add a due date (today due date)
        onView(withId(R.id.pick_date)).perform(click());
        UiObject okButton = mUiDevice.findObject(new UiSelector().text("OK"));
        try{
            okButton.click();
        }catch(UiObjectNotFoundException u){
            fail("Could not confirm date selection "+u.getMessage());
        }

        //TODO: When the issue of the location spinner during the test is solved, uncomment this.
        //add a location
        /*onView(withId(R.id.locationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Everywhere"))).perform(click());
        */
        onView(withId(R.id.edit_done_button_toolbar)).perform(click());
    }

    @Test
    public void tableRowDisplaysCorrectNumberOfUnfilledTasks(){
        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(typeText("unfTask 0"));
        pressBack();

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withId(R.id.number_of_unfilled_tasks)).check(matches(withText("1")));

        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(typeText("unfTask 1"));
        pressBack();

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withId(R.id.number_of_unfilled_tasks)).check(matches(withText("2")));
    }

    @Test
    public void CanSeeUnfilledTaskOnTheUnfilledActivity(){
        onView(withId(R.id.add_task_button)).perform(click());
        String titre = "unfTask 0";
        //add title
        onView(withId(R.id.title_task)).perform(typeText(titre));
        pressBack();

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withId(R.id.unfilled_task_button)).perform(click());

        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(0))
                .check(matches(hasDescendant(withText(titre))));
    }

    @Test
    public void canDeleteUnfilledTask(){

        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(typeText("unfTask 0"));
        pressBack();

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        String titleToCheck = "unfTask special";
        onView(withId(R.id.title_task)).perform(typeText(titleToCheck));
        pressBack();

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withId(R.id.unfilled_task_button)).perform(click());


        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeLeft()));

        onView(new TestRecyclerViewMatcher(R.id.list_view_tasks)
                .atPosition(0))
                .check(matches(hasDescendant(withText(titleToCheck))));

    }

    @Test
    public void unfilledTasksHaveNotDecidedInformation(){

        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        String titleToCheck = "unfTask";
        onView(withId(R.id.title_task)).perform(typeText(titleToCheck));
        pressBack();

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());

        onView(withId(R.id.unfilled_task_button)).perform(click());

        //opening taskInformation
        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //checking duration is "not decided yet"
        onView(withId(R.id.text_duration))
                .check(matches(withText(InstrumentationRegistry.getTargetContext().getString(R.string.unfilled_duration))));

    }

    @Test
    public void unfilledTaskAppearsAsPreviewOnTheMain(){

        String[] unfTasksTitle = {"unfTask1", "unfTask2", "unfTask3", "unfTask4"};

        for(String title: unfTasksTitle){
            addIncompleteTask(title);
        }

        onView(withId(R.id.unfilled_preview_1))
                .check(matches((withText(unfTasksTitle[0]))));

        onView(withId(R.id.unfilled_preview_2))
                .check(matches((withText(unfTasksTitle[1]))));

        onView(withId(R.id.unfilled_preview_4))
                .check(matches((withText(unfTasksTitle[2]))));

        onView(withId(R.id.unfilled_preview_3))
                .check(matches((withText(unfTasksTitle[3]))));
    }

    /**
     * with espresso action add an incomplete task(title only)
     *
     * @param title string the name of the task.
     */
    private void addIncompleteTask(String title){

        onView(withId(R.id.add_task_button)).perform(click());

        //add title
        onView(withId(R.id.title_task)).perform(typeText(title));
        pressBack();

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());
    }
}
