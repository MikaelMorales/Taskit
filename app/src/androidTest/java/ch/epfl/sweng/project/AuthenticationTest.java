package ch.epfl.sweng.project;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ch.epfl.sweng.project.authentication.LoginActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.uiautomator.UiDevice.getInstance;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.not;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class AuthenticationTest {

    /**
     * WARNING: Do not change the name of the test, or if you do so,
     * be sure to keep the alphabetic order, some tests need to be executed before others.
     */

    private final static String NEXT_BUTTON_ID = "identifierNext";
    private final static String PASSWORD_NEXT_ID = "passwordNext";
    private final static String ACCEPT_ID = "next";
    private static String mGoogleEmail;
    private static String mGooglePassword;
    private static long untilTimeout;
    private static final String TAG = "AuthenticationTest";
    private UiDevice mUiDevice;

    @Rule
    public final ActivityTestRule<LoginActivity> mActivityRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void setup() {
        mUiDevice = getInstance(getInstrumentation());
        mGoogleEmail = "trixyfinger@gmail.com";
        mGooglePassword = "sweng1234TaskIt";
        untilTimeout = 20000;
    }


    @Test
    public void authenticationFacebookCancelsIfInterrupted(){
        try{
            UiObject facebookButton = mUiDevice.findObject(new UiSelector().resourceId("ch.epfl.sweng.project:id/facebook_sign_in_button"));
            facebookButton.clickAndWaitForNewWindow();
        }catch(UiObjectNotFoundException i){
            fail("Could not find facebook button to click on it");
        }
        mUiDevice.pressBack();
        onView(withText(R.string.error_authentication_canceled))
                .inRoot(withDecorView(not((mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

    }


    /**
     * Click on the facebook sign in button,
     * check if the facebook login is launched upon it,
     * or if it grants immediately access to the mainActivity.
     */
    @Test
    public void facebookSignInGetLaunch() {
        try {
            UiObject facebookButton = mUiDevice.findObject(new UiSelector().resourceId("ch.epfl.sweng.project:id/facebook_sign_in_button"));
            facebookButton.click();
            Thread.sleep(untilTimeout);
            //the android.webkit.WebView launched by facebook should be the only to have those properties
            UiObject facebookWebLaunched = mUiDevice.findObject(new UiSelector().className("android.webkit.WebView"));
            facebookWebLaunched.click();
            assertTrue("Facebook login web window correctly launched", true);
        } catch (UiObjectNotFoundException u) {
            fail("Facebook window wasn't launched");
        }catch (java.lang.InterruptedException i){
            fail("There was an unexpected interruption during the launch");
        }
    }

    /**
     * perform user like actions on the phone to authenticate
     * oneself into a google account
     */
    //@Test
    public void googleLoginWorks() {
        onView(withId(R.id.google_sign_in_button)).perform(click());
        try{
            Thread.sleep(untilTimeout);
            associateNewGoogleAccount();
            checkIfActivity(R.id.add_task_button);
        }catch(java.lang.InterruptedException i){
            fail(i.getMessage());
        }catch(UiObjectNotFoundException u){
            u.printStackTrace();
        }
        checkIfActivity(R.id.add_location_button);
        mUiDevice.pressBack();
        mUiDevice.pressBack();
    }
    /**
     * perform user like actions on the phone to authenticate
     * oneself into a google account with a email non present
     * on the Firebase Database so lhe locationSettingActivity
     * gets launch.
     */
    @Test
    public void loginForTheFirstTimeLaunchLocationSettingsActivity() {

        mGoogleEmail = "cirdec3961@gmail.com";
        mGooglePassword = "Kristel99";
        try{
            UiObject googleButton = mUiDevice.findObject(new UiSelector().resourceId("ch.epfl.sweng.project:id/google_sign_in_button"));
            googleButton.clickAndWaitForNewWindow();
            try{
                Thread.sleep(untilTimeout);
                associateNewGoogleAccount();
            }catch(java.lang.InterruptedException i){
                fail(i.getMessage());
            }catch(UiObjectNotFoundException ignored){
            }
            checkIfActivity(R.id.add_location_button);
            mUiDevice.pressBack();
            mUiDevice.pressBack();
        }catch(UiObjectNotFoundException u){
            fail("Could not get googleButton to click on it "+u.getMessage());
        }
    }

    /**
     * method that proceeds to go through all the authentication process
     * when it is the first time we try to connect a google account with the app.
     *
     * The test might takes some time after clicking on the sign in button,
     * the transition between espresso and uiautomator might be responsible
     * for that.
     */
    private void associateNewGoogleAccount() throws UiObjectNotFoundException {

        UiObject emailHint = mUiDevice.findObject(new UiSelector().resourceId("identifierId"));
        emailHint.setText(mGoogleEmail);

        try{

            UiObject nextAction = mUiDevice.findObject(new UiSelector().resourceId(NEXT_BUTTON_ID));
            nextAction.clickAndWaitForNewWindow();

            UiObject passwordHint = mUiDevice.findObject(new UiSelector().resourceId("password"));
            passwordHint.setText(mGooglePassword);

            nextAction = mUiDevice.findObject(new UiSelector().resourceId(PASSWORD_NEXT_ID));
            nextAction.clickAndWaitForNewWindow();

            UiObject acceptAction = mUiDevice.findObject(new UiSelector().resourceId(ACCEPT_ID));
            acceptAction.clickAndWaitForNewWindow();

            UiScrollable googleServices = new UiScrollable(new UiSelector().scrollable(true));
            googleServices.scrollForward();

            UiObject nextGoogleServer = mUiDevice.findObject(new UiSelector().text("NEXT"));
            nextGoogleServer.clickAndWaitForNewWindow();

        }catch (UiObjectNotFoundException u ){
            fail("There was an error while trying to associate new Google account with the app.");
        }
    }

    public void removeAccount(){
        try{
            mUiDevice.pressHome();
            //click on main button
            //index : 2, package : com.android.launcher3, content_description: Apps
            UiObject UiAppTray = mUiDevice.findObject(new UiSelector().descriptionContains("Apps"));
            UiAppTray.click();
            UiObject UiSettingsIcon = mUiDevice.findObject(new UiSelector().text("Settings"));
            UiSettingsIcon.clickAndWaitForNewWindow();

            //scroll the settings
            UiScrollable settingsView = new UiScrollable(new UiSelector().scrollable(true));
            //put the scroller on the up most position to scrollForward correctly after.
            settingsView.flingBackward();
            settingsView.scrollForward();
            settingsView.scrollForward();

            UiObject UiAccount = mUiDevice.findObject(new UiSelector().text("Accounts"));
            UiAccount.click();

            UiObject GoogleAccount = mUiDevice.findObject(new UiSelector().text("Google"));
            try{
                GoogleAccount.click();
                UiObject threeDots = mUiDevice.findObject(new UiSelector().descriptionContains("More options"));
                try{

                    threeDots.click();

                    UiObject deleteAccountOption = mUiDevice.findObject(new UiSelector().text("Remove account"));
                    deleteAccountOption.click();

                    UiObject removeCurrAccount = mUiDevice.findObject(new UiSelector().text("REMOVE ACCOUNT"));
                    removeCurrAccount.click();
                }catch(UiObjectNotFoundException unattended){
                    Log.d(TAG, "For some reason, could not remove the account, please do it manually");
                }
            }catch(UiObjectNotFoundException u){
                //the account does not exist.
            }
        }catch (UiObjectNotFoundException e){
            Log.d(TAG, "Error, something UI related not found.");
        }
    }

    /**
     * serves as only checking if the add button  of the activity
     * we want to check is displayed,
     * which is equivalent as checking if we reached mainActivity
     *
     * @param id the id of the button to check
     */
    private void checkIfActivity(int id){
        try{
            Thread.sleep(untilTimeout);
        }catch(java.lang.InterruptedException e){
            Log.d(TAG, "There was a problem during the changing of activity");
        }
        onView(withId(id)).check(matches(isDisplayed()));
    }
}