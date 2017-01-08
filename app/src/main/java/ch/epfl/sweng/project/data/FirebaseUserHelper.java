package ch.epfl.sweng.project.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.project.Location;
import ch.epfl.sweng.project.User;
import ch.epfl.sweng.project.Utils;

/**
 * Proxy that does all the work between the app and the firebase real time database.
 * It allows the user to fetch his predefined favorite locations from the
 * database.
 *
 * Note: The queries are done asynchronously
 */
public class FirebaseUserHelper implements UserHelper {

    @Override
    public User retrieveUserInformation(User currentUser, Iterable<DataSnapshot> snapshots) {
        List<Location> listLocations = new ArrayList<>();
        //Construct each user's location
        for (DataSnapshot data : snapshots) {
            String name = data.child("name").getValue(String.class);
            Double latitude = data.child("latitude").getValue(Double.class);
            Double longitude = data.child("longitude").getValue(Double.class);
            //Create location
            Location location = new Location(name, latitude, longitude);
            //Add the location to the list
            listLocations.add(location);
        }
        //Set the list with the user's location list
        currentUser.setListLocations(listLocations);
        return currentUser;
    }


    /**
     * Deleter a user on the database
     *
     * @param user The user to be deleted
     */
    private static void deleteUser(User user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = mDatabase.child("users").child(Utils.encodeMailAsFirebaseKey(user.getEmail())).getRef();
        userRef.removeValue();
    }

    /**
     * Add a user on the database
     *
     * @param user The user to be added
     */
    public static void addUser(User user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = mDatabase.child("users").child(Utils.encodeMailAsFirebaseKey(user.getEmail())).getRef();
        userRef.setValue(user);
    }

    /**
     * Edit a user on the database
     *
     * @param user The user that need to be updated
     */
    public static void updateUser(User user) {
        deleteUser(user);
        addUser(user);
    }
}
