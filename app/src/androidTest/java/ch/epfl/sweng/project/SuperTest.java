package ch.epfl.sweng.project;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;

import org.junit.BeforeClass;

import ch.epfl.sweng.project.data.TaskProvider;
import ch.epfl.sweng.project.data.UserProvider;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.uiautomator.UiDevice.getInstance;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class SuperTest {
    final int createdTask = 2;

    @BeforeClass
    public static void setTaskProvider() {
        TaskProvider.setProvider(Utils.TEST_PROVIDER);
    }

    @BeforeClass
    public static void setUserProvider() {
        UserProvider.setProvider(Utils.TEST_PROVIDER);
    }

    static void checkALocation(String locationTitle, int locationPos){
        onData(anything())
                .inAdapterView(withId(R.id.list_view_locations))
                .atPosition(locationPos)
                .check(matches(hasDescendant(withText(locationTitle))));
    }

    static void scrollDown(){
        //Check that the title has been updated
        UiScrollable scrollDown = new UiScrollable(new UiSelector().scrollable(true));
        try{
            scrollDown.scrollForward();
        }catch(UiObjectNotFoundException u){
            fail("Could not scroll down on location settings view : "+u.getMessage());
        }
    }

    void createALocation(String locationTitle){
        onView(withId(R.id.add_location_button)).perform(click());
        onView(withId(R.id.locationName)).perform(typeText(locationTitle));
        pressBack();
        onView(withId(R.id.location_done_button_toolbar)).perform(click());
    }

    void createATask(String taskTitle, String taskDescription){

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

        //TODO: When the issue of the location spinner during the test is solved, uncomment this.
        //add a location
        /*onView(withId(R.id.locationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Everywhere"))).perform(click());
        */

        //add the description
        /*onView(withId(R.id.description_task)).perform(typeText(taskDescription));
        pressBack();*/
        //For now, create task with empty description
        onView(withId(R.id.edit_done_button_toolbar)).perform(click());
    }

    void deleteALocation(int position){
        onData(anything())
                .inAdapterView(withId(R.id.list_view_locations))
                .atPosition(position).perform(longClick());
        onView(withText(R.string.flt_ctx_menu_delete)).perform(click());
    }

    void editALocation(int position){
        onData(anything())
                .inAdapterView(withId(R.id.list_view_locations))
                .atPosition(position).perform(longClick());
        onView(withText(R.string.flt_ctx_menu_edit)).perform(click());
    }


    /**
     * Utility method to wait until we can check which activity was launched.
     */
    static void waitForActivity(){
        try{
            Thread.sleep(3000);
        }catch(java.lang.InterruptedException i){
            fail(i.getMessage());
        }
    }

    /**
     *  Delete the numbers of tasks given
     */
    void emptyDatabase(int size) {
        for (int i = 0; i < size; i++) {
            onView(withId(R.id.list_view_tasks))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeLeft()));
        }
    }
}
