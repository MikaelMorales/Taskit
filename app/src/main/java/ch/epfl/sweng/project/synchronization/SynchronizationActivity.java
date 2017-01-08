package ch.epfl.sweng.project.synchronization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Map;

import ch.epfl.sweng.project.MainActivity;
import ch.epfl.sweng.project.R;
import ch.epfl.sweng.project.User;
import ch.epfl.sweng.project.Utils;
import ch.epfl.sweng.project.data.UserProvider;

/**
 * Activity that synchronize the user location before
 * reaching MainActivity.
 */
public class SynchronizationActivity extends Activity {

    private SynchronizedQueries synchronizedQueries;

    /**
     * Override the onCreate method
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronization);
        String mail;
        try {
            mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } catch (NullPointerException e) {
            mail = User.DEFAULT_EMAIL;
        }

        User currentUser = new User(mail);

        switch (UserProvider.mProvider) {
            case Utils.FIREBASE_PROVIDER:
                //Get reference of the database
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                //Get reference of the user
                Query userRef = mDatabase.child("users")
                        .child(Utils.encodeMailAsFirebaseKey(currentUser.getEmail()))
                        .child("listLocations").getRef();

                //This class allows us to get all the user's data before continuing executing the app
                synchronizedQueries = new SynchronizedQueries(userRef);
                final com.google.android.gms.tasks
                        .Task<Map<Query, DataSnapshot>> readFirebaseTask = synchronizedQueries.start();
                //Listener that listen when communications with firebase end
                readFirebaseTask.addOnCompleteListener(this,
                        new UserAllOnCompleteListener(userRef, currentUser, getApplicationContext()));
                break;

            case Utils.TEST_PROVIDER:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

            default:
                throw new IllegalStateException("UserProvider not in FIREBASE_PROVIDER nor in TEST_PROVIDER");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(UserProvider.mProvider.equals(Utils.FIREBASE_PROVIDER)) {
            synchronizedQueries.stop();
        }
    }
}
