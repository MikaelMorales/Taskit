package ch.epfl.sweng.project;

import android.location.Location;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests!
 */
@RunWith(AndroidJUnit4.class)
public class GeolocationTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public GeolocationTest() {
        super(MainActivity.class);
    }

    @Test
    public void testOnLocationChanged() {
        android.location.Location location = new Location("Sion");
        location.setLatitude(14.00);
        location.setLongitude(43.00);
    }
}