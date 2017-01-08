package ch.epfl.sweng.project.location_setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

import ch.epfl.sweng.project.Location;
import ch.epfl.sweng.project.MainActivity;
import ch.epfl.sweng.project.R;
import ch.epfl.sweng.project.User;
import ch.epfl.sweng.project.authentication.LoginActivity;
import ch.epfl.sweng.project.data.FirebaseUserHelper;
import ch.epfl.sweng.project.synchronization.SynchronizationActivity;

/**
 * Class where the user can see his favorite locations
 */
public class LocationSettingActivity extends AppCompatActivity {
    public static final String USER_KEY = "ch.epfl.sweng.MainActivity.CURRENT_USER";
    private final int newLocationRequestCode = 1;
    private LocationFragment fragment;
    private SharedPreferences prefs;
    private static User currentUser;
    private boolean firstConnection;

    /**
     * Override the onCreate method
     * Initializes the buttons and fields and retrieves the user information
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_setting);
        prefs = getApplicationContext().getSharedPreferences(getString(R.string.application_prefs_name), MODE_PRIVATE);
        fragment = new LocationFragment();
        firstConnection = prefs.contains(getString(R.string.new_user))
                && prefs.getBoolean(getString(R.string.new_user), true);

        if(!firstConnection) {
            //if accessed from settings, load custom locations
            currentUser = MainActivity.getUser();
            Bundle bundle = new Bundle();
            bundle.putParcelable(USER_KEY, currentUser);
            fragment.setArguments(bundle);
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.locations_container, fragment)
                    .commit();
        }

        //Initialize the toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.location_setting_toolbar);
        initializeToolbar(mToolbar);

        ImageButton doneLocationSettingButton = (ImageButton) findViewById(R.id.location_setting_done_button_toolbar);

        doneLocationSettingButton.setOnClickListener(new LocationSettingActivity.OnDoneButtonClickListener());
    }

    /**
     * Method called when add_location_button is clicked.
     * It start a NewLocationActivity with startActivityForResult
     *
     * @param v Required argument
     */
    public void openNewLocationActivity(View v) {
        Intent intent = new Intent(this, NewLocationActivity.class);
        intent.putParcelableArrayListExtra(LocationFragment.LOCATIONS_LIST_KEY, (ArrayList<Location>) fragment.getLocationList());
        startActivityForResult(intent, newLocationRequestCode);
    }

    /**
     * Method called when an activity launch inside MainActivity,
     * is finished. This method is triggered only if we use
     * startActivityForResult.
     *
     * @param requestCode The integer request code supplied to startActivityForResult
     *                    used as an identifier.
     * @param resultCode  The integer result code returned by the child activity
     * @param data        An intent which can return result data to the caller.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == newLocationRequestCode) {
            if (resultCode == RESULT_OK) {
                // Get result from the result intent.
                Location newLocation = data.getParcelableExtra(NewLocationActivity.RETURNED_LOCATION);
                // Add element to the listLocation
                fragment.addLocation(newLocation);
                if (!firstConnection) {
                    currentUser = new User(currentUser.getEmail(), fragment.getLocationList());
                    FirebaseUserHelper.updateUser(currentUser);
                    MainActivity.setUser(currentUser);
                }
            }
        }
    }

    /**
     * Start the toolbar and enable that back button on the toolbar.
     *
     * @param mToolbar the toolbar of the activity
     */
    private void initializeToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    /**
     * Create the intent that will be sent back.
     */
    private void resultActivity() {
        if(prefs.getBoolean(getString(R.string.new_user), true)) {
            Bundle extras = getIntent().getExtras();
            final String userEmail = extras.getString(LoginActivity.USER_EMAIL_KEY);
            User user = new User(userEmail, fragment.getLocationList());
            FirebaseUserHelper.addUser(user);
            prefs.edit().putBoolean(getString(R.string.new_user), false).apply();
            Intent intent = new Intent(LocationSettingActivity.this, SynchronizationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    /**
     * Listener of the done edit button.
     */
    private class OnDoneButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            resultActivity();
            finish();
        }
    }
}