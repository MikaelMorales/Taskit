package ch.epfl.sweng.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import ch.epfl.sweng.project.location_setting.LocationSettingActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.IsNot.not;


/**
 * Unit tests!
 */
@RunWith(AndroidJUnit4.class)
public final class LocationSettingTest extends SuperTest {


    @Rule
    public final ExpectedException thrownException = ExpectedException.none();
    @Rule
    public ActivityTestRule<LocationSettingActivity> mActivityRule = new ActivityTestRule<LocationSettingActivity>(
            LocationSettingActivity.class){
        //Override to be able to change the SharedPreferences effectively
        @Override
        protected void beforeActivityLaunched(){
            Context actualContext = InstrumentationRegistry.getTargetContext();
            SharedPreferences prefs = actualContext.getSharedPreferences(
                    actualContext.getString(R.string.application_prefs_name),
                    Context.MODE_PRIVATE);
            prefs.edit().putBoolean(actualContext.getString(R.string.new_user), true).apply();
            super.beforeActivityLaunched();
        }
    };
    private String mTitleToBeTyped;
    private String mOldTitle;


    @Before
    public void initValidString() {
        mTitleToBeTyped = "test title number ";
        mOldTitle = "old title";
    }



    /**
     * Test that those locations are by default present
     * at the top of the location setting when first opening.
     */
    @Test
    public void defaultLocationsArePresent(){
        checkALocation("Home", 0);
    }

    @Test
    public void someDefaultLocationsCanBeDeletedOtherNot(){
        //deleting the "optional" default locations and testing they are not there.
        deleteALocation(0);
        checkALocation("Office", 0);
    }

    /**
     * Test that a Location has been correctly created and added
     * in the ListView.
     */
    @Test
    public void testCanAddLocation() {
            createALocation(mTitleToBeTyped);
            scrollDown();
            //Check title name inside listView
            checkALocation(mTitleToBeTyped, 3);
    }




    @Test
    public void testCanEditLocation() {
        final int locationPosition = 3;
        //Create a location
        createALocation(mOldTitle);
        scrollDown();
        //Try to edit the first location to put the same title as the first location
        onData(anything())
                .inAdapterView(withId(R.id.list_view_locations))
                .atPosition(locationPosition).perform(longClick());
        onView(withText(R.string.flt_ctx_menu_edit)).perform(click());
        //Update the title and the description
        onView(withId(R.id.locationName)).perform(clearText());
        onView(withId(R.id.locationName)).perform(typeText(mTitleToBeTyped));
        pressBack();

        onView(withId(R.id.location_done_button_toolbar)).perform(click());

        scrollDown();
        //Check that the title has been updated
        onData(anything())
                .inAdapterView(withId(R.id.list_view_locations))
                .atPosition(locationPosition)
                .check(matches(hasDescendant(withText(mTitleToBeTyped))));
        //delete the location for further tests.
        deleteALocation(locationPosition);
    }

    /**
     * Test that we can't put an existing title when editing a location.
     */
    @Test
    public void testCannotEditLocationWithAlreadyExistingTitle() {
        final int locationPosition = 3;
        //Create a location
        createALocation(mOldTitle);
        scrollDown();
        //Try to edit the first location to put the same title as a default location
        onData(anything())
                .inAdapterView(withId(R.id.list_view_locations))
                .atPosition(locationPosition).perform(longClick());
        onView(withText(R.string.flt_ctx_menu_edit)).perform(click());
        //Update the title and the description
        onView(withId(R.id.locationName)).perform(clearText());
        onView(withId(R.id.locationName)).perform(typeText("Home"));
        pressBack();


        //Get the error message
        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.error_location_name_duplicated)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.location_name_layout))
                .check(matches(TestErrorTextInputLayoutMatcher
                        .withErrorText(containsString(errorMessage))));

        //Go back to the main activity for the next test
        pressBack();

        //delete the location for further tests.
        deleteALocation(locationPosition);

    }

    /**
     * Test that we can't add a location with an already used title.
     */
    @Test
    public void testCannotAddLocationWithExistingTitle() {

        //Try to create a second class with the same title as the first one
        onView(withId(R.id.add_location_button)).perform(click());
        onView(withId(R.id.locationName)).perform(typeText("Everywhere"));
        pressBack();
        onView(withId(R.id.location_done_button_toolbar)).check(matches(not(isDisplayed())));

        //Get the error message
        String errorMessage = getInstrumentation()
                .getTargetContext()
                .getResources()
                .getText(R.string.error_location_name_duplicated)
                .toString();

        //Check that the error message is displayed
        onView(withId(R.id.location_name_layout))
                .check(matches(TestErrorTextInputLayoutMatcher
                        .withErrorText(containsString(errorMessage))));
        pressBack();
    }
}
