package ch.epfl.sweng.project.synchronization;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.epfl.sweng.project.Location;
import ch.epfl.sweng.project.MainActivity;
import ch.epfl.sweng.project.User;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * OnCompleteListener that operates when the the data are retrieve from Firebase.
 */
public class UserAllOnCompleteListener implements OnCompleteListener<Map<Query, DataSnapshot>> {
    public static final String CURRENT_USER_KEY =
            "ch.epfl.sweng.project.complete_listener.UserAllOnCompleteListener.CURRENT_USER_KEY";

    private final Query userRef;
    private final User currentUser;
    private final Context synchronizationActivityContext;

    /**
     * Constructor of the class
     *
     * @param userRef The reference of the user in Firebase
     * @param currentUser The current user
     * @param synchronizationActivityContext The context of SynchronizationActivity
     */
    public UserAllOnCompleteListener(@NonNull Query userRef, @NonNull User currentUser,
                                     @NonNull Context synchronizationActivityContext) {
        super();
        this.userRef = userRef;
        this.currentUser = currentUser;
        this.synchronizationActivityContext = synchronizationActivityContext;
    }

    @Override
    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Map<Query, DataSnapshot>> task) {
        if (task.isSuccessful()) {
            final Map<Query, DataSnapshot> UserResult = task.getResult();
            retrieveUserInformation(UserResult.get(userRef).getChildren());
            launchNextActivity();
        } else {
            try {
                task.getException();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method that constructs the user's locations from the DataSnapshot.
     *
     * @param snapshots The DataSnapshot
     */
    private void retrieveUserInformation(Iterable<DataSnapshot> snapshots) {
        List<Location> listLocations = new ArrayList<>();
        //Construct each user's location
        for (DataSnapshot data : snapshots) {
            String name = (String) data.child("name").getValue();
            Double latitude = data.child("latitude").getValue(Double.class);
            Double longitude = data.child("longitude").getValue(Double.class);
            //Create location
            Location location = new Location(name, latitude, longitude);
            //Add the location to the list
            listLocations.add(location);
        }
        //Set the list with the user's location list
        currentUser.setListLocations(listLocations);
    }

    /**
     * Method that set the intent used to launch the next activity (MainActivity)
     */
    private void launchNextActivity() {
        Intent intent = new Intent(synchronizationActivityContext, MainActivity.class);
        intent.putExtra(UserAllOnCompleteListener.CURRENT_USER_KEY, currentUser);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
        synchronizationActivityContext.startActivity(intent);
    }
}
