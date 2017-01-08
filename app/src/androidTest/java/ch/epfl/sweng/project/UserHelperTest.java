package ch.epfl.sweng.project;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.project.data.UserHelper;
import ch.epfl.sweng.project.data.UserProvider;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UserHelperTest extends SuperTest {

    private User user;
    private UserHelper provider;

    @Before
    public void initUser() {
        user = new User(User.DEFAULT_EMAIL);
        provider = new UserProvider().getUserProvider();
    }

    @Test
    public void localProviderReturnsTheCorrectUser() {
        User currentUser = provider.retrieveUserInformation(null, null);

        assertEquals(user.getEmail(), currentUser.getEmail());
        assertEquals(user.getListLocations().size(), currentUser.getListLocations().size());
        for(int i=0; i < user.getListLocations().size(); i++) {
            assertEquals(user.getListLocations().get(i).getName(),
                    currentUser.getListLocations().get(i).getName());
        }
    }
}
