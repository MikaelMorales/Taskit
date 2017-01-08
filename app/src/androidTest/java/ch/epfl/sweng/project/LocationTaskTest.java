package ch.epfl.sweng.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ch.epfl.sweng.project.data.UserProvider;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.uiautomator.UiDevice.getInstance;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;

/**
 * test the location modification or deletion and the update required for the task using this location
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class LocationTaskTest extends SuperTest {

    @BeforeClass
    public static void setUserProvider() {
        UserProvider.setProvider(Utils.TEST_PROVIDER);
    }
    private UiDevice mUiDevice;

    @Before
    public void setup() {
        mUiDevice = getInstance(getInstrumentation());
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(
            MainActivity.class){
        //Override to be able to change the SharedPreferences effectively
        @Override
        protected void beforeActivityLaunched(){
            Context actualContext = InstrumentationRegistry.getTargetContext();
            SharedPreferences prefs = actualContext.getSharedPreferences(actualContext.getString(R.string.application_prefs_name), Context.MODE_PRIVATE);
            prefs.edit().putBoolean(actualContext.getString(R.string.first_launch), false).apply();
            prefs.edit().putBoolean(actualContext.getString(R.string.new_user), false).apply();
            super.beforeActivityLaunched();
        }
    };

    public void addNewLocation() {
        String LOCATION_MARS = "Mars";
        createALocation(LOCATION_MARS);
        String LOCATION_EVEREST = "Everest";
        createALocation(LOCATION_EVEREST);
    }

    public void addNewTask() {
        String marsTask = "search for water";
        String everestTask = "climb the everest";
        addTask(marsTask, R.id.energy_low, 1, 3);
        addTask(everestTask, R.id.energy_normal, 2, 4);
    }


    /**
     * check that when we remove a custom location that is used in some current tasks,
     * then the app need to ask us to change the location of this task,
     * and this test verify that those tasks has been updated with the new location
     */
    @Test
    public void deleteALocationUsedInATaskMustAskToChangeLocationAndChangeItCorrectly(){
        SuperTest.waitForActivity();

        // go to the settings activity
        Espresso.openContextualActionModeOverflowMenu();
        onData(anything())
                .atPosition(0)
                .perform(click());

        // go to the location settings
        onView(withId(R.id.settings_text_locations)).perform(click());
        onView(withId(R.id.location_setting_done_button_toolbar)).check(matches(isDisplayed()));

        addNewLocation();
        mUiDevice.pressBack();
        mUiDevice.pressBack();

        addNewTask();

        // go to the settings activity
        Espresso.openContextualActionModeOverflowMenu();
        onData(anything())
                .atPosition(0)
                .perform(click());

        // go to the location settings
        onView(withId(R.id.settings_text_locations)).perform(click());
        onView(withId(R.id.location_setting_done_button_toolbar)).check(matches(isDisplayed()));

        // remove a location that is used by a task /!\ index for the custom location
        deleteALocation(0);

        onView(withText("Used location")).perform(click());
        onData(anything())
                .atPosition(0)
                .perform(click());

        // select the new location
        UiObject selectLocation = mUiDevice.findObject(new UiSelector().text("Downtown"));
        try {
            selectLocation.click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        onView(withText("OK")).perform(click());
        onView(withId(R.id.location_setting_done_button_toolbar)).perform(click());
        mUiDevice.pressBack();

        // check that the marsTask is now set to the new location
        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        onView(withText("Downtown")).check(matches(isDisplayed()));
    }

    @Test
    public void editALocationUsedInATaskMustAskToChangeLocationAndChangeItCorrectly(){
        SuperTest.waitForActivity();

        // go to the settings activity
        Espresso.openContextualActionModeOverflowMenu();
        onData(anything())
                .atPosition(0)
                .perform(click());

        // go to the location settings
        onView(withId(R.id.settings_text_locations)).perform(click());
        onView(withId(R.id.location_setting_done_button_toolbar)).check(matches(isDisplayed()));

        addNewLocation();
        mUiDevice.pressBack();
        mUiDevice.pressBack();

        addNewTask();

        // go to the settings activity
        Espresso.openContextualActionModeOverflowMenu();
        onData(anything())
                .atPosition(0)
                .perform(click());

        // go to the location settings
        onView(withId(R.id.settings_text_locations)).perform(click());
        onView(withId(R.id.location_setting_done_button_toolbar)).check(matches(isDisplayed()));

        // remove a location that is used by a task /!\ index for the custom location
        editALocation(0);

        onView(withId(R.id.locationName)).perform(clearText());
        onView(withId(R.id.locationName)).perform(typeText("Jupiter"));
        onView(withId(R.id.location_done_button_toolbar)).perform(click());

        onView(withId(R.id.location_setting_done_button_toolbar)).perform(click());
        mUiDevice.pressBack();

        // check that the marsTask is now set to the new location
        onView(withId(R.id.list_view_tasks))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        onView(withText("Jupiter")).check(matches(isDisplayed()));
    }

    private void addTask(String taskName, int radioButtonId, int indexDuration, int indexLocation) {
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

        // select the location
        onView(withId(R.id.locationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(indexLocation).perform(click());

        onView(withId(R.id.edit_done_button_toolbar)).perform(click());
    }


    @Test
    public void returnButtonShouldLeadToMainActivity(){
        SuperTest.waitForActivity();

        // go to the settings activity
        Espresso.openContextualActionModeOverflowMenu();
        onData(anything())
                .atPosition(0)
                .perform(click());

        onView(withContentDescription("Navigate up")).perform(click());
        onView(withId(R.id.add_task_button)).check(matches(isDisplayed()));
    }

}
