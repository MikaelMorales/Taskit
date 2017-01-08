package ch.epfl.sweng.project;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

public class ChatTest extends SuperTest {

    private final String message = "Hello";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

   private void openChat() {
       final String name = "Piano";
       final String description = "Clavecin";
       createATask(name, description);
       //Open EditTaskActivity for the newly created task
       onView(withId(R.id.list_view_tasks))
               .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
       onView(withId(R.id.open_chat)).perform(click());
   }

    @Test
    public void messageSendIsDisplayed() {
        openChat();
        onView(withId(R.id.input)).perform(typeText(message));
        onView(withId(R.id.send_message_button)).perform(click());
        onView(withId(R.id.list_of_messages)).check(matches(hasDescendant(withText(message))));
        pressBack();
        pressBack();
        onView(withId(R.id.trash_menu)).perform(click());
    }

    @Test
    public void buttonIsDisableByDefault() {
        openChat();
        onView(withId(R.id.send_message_button)).check(matches(not(isEnabled())));
        pressBack();
        onView(withId(R.id.trash_menu)).perform(click());
    }

    @Test
    public void buttonIsEnableWhenWriting() {
        openChat();
        onView(withId(R.id.send_message_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.input)).perform(typeText(message));
        onView(withId(R.id.send_message_button)).check(matches(isEnabled()));
        pressBack();
        pressBack();
        onView(withId(R.id.trash_menu)).perform(click());
    }

    @Test
    public void buttonDisableWithEmptyText() {
        openChat();
        onView(withId(R.id.send_message_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.input)).perform(typeText(message));
        onView(withId(R.id.send_message_button)).check(matches(isEnabled()));
        onView(withId(R.id.input)).perform(clearText());
        onView(withId(R.id.send_message_button)).check(matches(not(isEnabled())));
        pressBack();
        pressBack();
        onView(withId(R.id.trash_menu)).perform(click());
    }
}
