package ch.epfl.sweng.project;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Unit tests!
 */
@RunWith(AndroidJUnit4.class)
public class UserTest {
    @Rule
    public final ExpectedException thrownException = ExpectedException.none();
    private User testUser1; // with first constructor
    private User testUser2; // with second constructor

    @Before
    public void initValidValues() {
        String email = "trixyfinger@gmail.com";
        Location location1 = new Location("Office", 80, 89);
        Location location2 = new Location("Home", 80, 89);
        List<Location> listLocations = new ArrayList<>();
        listLocations.add(location2);
        listLocations.add(location1);
        testUser1 = new User(email);
        testUser2 = new User(email, listLocations);
    }

    @Test
    public void testFirstConstructor() {

        List<Location> listLocations;
        // Default values:
        listLocations = Arrays.asList(new Location(), new Location());

        String email = "trixyfinger@gmail.com";

        User userTest = new User(email);

        assertEquals(email, userTest.getEmail());
        assertEquals(listLocations.get(0).getLatitude(), userTest.getListLocations().get(0).getLatitude());
        assertEquals(listLocations.get(1).getName(), userTest.getListLocations().get(1).getName());
    }

    /**
     * Test that the constructor and the getters work correctly.
     */
    @Test
    public void testSecondConstructor() {
        String email = "trixyfinger@gmail.com";
        String locationNameTest1 = "locationName test workplace";
        double latTest1 = 32;
        double longTest1 = 55;
        Location locationTest1 = new Location(locationNameTest1, latTest1, longTest1);
        String locationNameTest2 = "locationName test home";
        double latTest2 = 43;
        double longTest2 = 90;
        Location locationTest2 = new Location(locationNameTest2, latTest2, longTest2);

        List<Location> listLocations = new ArrayList<>();
        listLocations.add(locationTest1);
        listLocations.add(locationTest2);

        User userTest = new User(email, listLocations);

        assertEquals(email, userTest.getEmail());
        assertEquals(listLocations.get(0).getName(), userTest.getListLocations().get(0).getName());
        assertEquals(listLocations.get(1).getLatitude(), userTest.getListLocations().get(1).getLatitude());
    }

    /**
     * Test the setter setListLocations
     */
    @Test
    public void testSetterListLocations() {
        String locationNameTest1 = "locationName test workplace";
        double latTest1 = 32;
        double longTest1 = 55;
        Location locationTest1 = new Location(locationNameTest1, latTest1, longTest1);
        String locationNameTest2 = "locationName test home";
        double latTest2 = 43;
        double longTest2 = 90;
        Location locationTest2 = new Location(locationNameTest2, latTest2, longTest2);

        List<Location> listLocations = new ArrayList<>();
        listLocations.add(locationTest1);
        listLocations.add(locationTest2);
        testUser1.setListLocations(listLocations);

        assertEquals(latTest1, testUser1.getListLocations().get(0).getLatitude());
        assertEquals(locationNameTest2, testUser1.getListLocations().get(1).getName());
    }


    /**
     * Test updateListLocation
     */
    @Test
    public void testUpdateListLocation() {
        Location newTestLocation = new Location("Office", 34, 43);
        testUser2.updateLocation(newTestLocation);

        assertEquals(newTestLocation.getName(), testUser2.getListLocations().get(1).getName());
    }

    /**
     * Test that the exception of setListLocations is thrown
     */
    @Test
    public void testSetterListLocationsException() {
        thrownException.expect(IllegalArgumentException.class);
        testUser2.setListLocations(null);
    }

    /**
     * Test that the exceptions of updateLocation are thrown
     */
    @Test
    public void testUpdateLocationExceptions() {
        thrownException.expect(IllegalArgumentException.class);
        testUser2.updateLocation(null);

        Location newTestLocation = new Location("Everywhere", 43, 43);
        thrownException.expect(IllegalArgumentException.class);
        testUser2.updateLocation(newTestLocation);
    }

    /**
     * Test that the exception of the second constructor of User is thrown
     */
    @Test
    public void testSecondConstructorException() {
        String email = "trixyfinger@gmail.com";

        thrownException.expect(NullPointerException.class);
        new User(email, null);
    }
}

