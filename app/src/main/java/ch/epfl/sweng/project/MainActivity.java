package ch.epfl.sweng.project;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sweng.project.authentication.LoginActivity;
import ch.epfl.sweng.project.data.FirebaseTaskHelper;
import ch.epfl.sweng.project.data.UserProvider;
import ch.epfl.sweng.project.settings.SettingsActivity;
import ch.epfl.sweng.project.synchronization.UserAllOnCompleteListener;


/**
 * MainActivity
 */
public final class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String USER_KEY = "ch.epfl.sweng.MainActivity.CURRENT_USER";
    public static final String UNFILLED_TASKS = "ch.epfl.sweng.MainActivity.UNFILLED_TASKS";
    private static final int UNFILLED_TASKS_DIGEST_NBR = 4;

    private final int newTaskRequestCode = 1;
    private final int unfilledTaskRequestCode = 2;
    private static FilledTaskFragment mainFragment;
    private Context mContext;

    //stock unfilledTasks
    private static ArrayList<Task> unfilledTasks;

    private Intent intent;
    private static User currentUser;

    // Will be used later on
    private static String userLocation;
    private static String detectedUserLocation = "";
    private static int userTimeAtDisposal;

    private Spinner mLocation;
    private Spinner mDuration;

    private ArrayAdapter<String> locationAdapter;
    private ArrayAdapter<String> durationAdapter;

    public static Map<Integer, String> DURATION_MAP;
    public static Map<String, Integer> REVERSE_DURATION;
    private static Map<Integer, String> START_DURATION_MAP;
    private static Map<String, Integer> REVERSE_START_DURATION;
    public static Map<Integer, String> ENERGY_MAP;
    private static Map<String, Integer> REVERSE_ENERGY;

    private TableRow unfilledTaskButton;

    // Geolocation variables:
    private GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_LOCATION = 2;

    public static boolean unfilledSyncFinished;

    private final String TAG = "Location API";

    /**
     * Override the onCreate method to create a FilledTaskFragment
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    @TargetApi(Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Facebook SDK, in order to logout correctly
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Initialize googleApiClient that will trigger the geolocation part
        createGoogleApiClient();


        setContentView(R.layout.activity_main);
        getSupportActionBar().setIcon(R.mipmap.new_logo);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //If we are not in test mode
        //We get the user that was loaded in SynchronisationActivity
        switch (UserProvider.mProvider) {
            case Utils.FIREBASE_PROVIDER:
                intent = getIntent();
                checkIntent();
                currentUser = intent.getParcelableExtra(UserAllOnCompleteListener.CURRENT_USER_KEY);
                checkIntentExtra();
                break;

            case Utils.TEST_PROVIDER:
                unfilledSyncFinished = true;
                currentUser = new User(User.DEFAULT_EMAIL);
                break;

            default:
                throw new IllegalStateException("UserProvider not in FIREBASE_PROVIDER nor in TEST_PROVIDER");
        }

        mContext = getApplicationContext();

        createUtilityMaps();

        //retrieving unfilled task to put them on the unfilled table row.
        unfilledTasks = new ArrayList<>();
        unfilledSyncFinished = false;
        FirebaseTaskHelper.retrieveUnfilledFromMain(currentUser, unfilledTasks);

        //Add the user to TaskFragments
        mainFragment = new FilledTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(USER_KEY, currentUser);
        mainFragment.setArguments(bundle);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.tasks_container, mainFragment)
                    .commit();
        }

        //Default values
        userLocation = getResources().getString(R.string.select_one);
        userTimeAtDisposal = 120; //2 hours

        initializeAdapters();
        //to be able to synchronize the data retrieving from the unfilled tasks.
        Handler uiHandler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //Handle the table row in case of unfinished tasks
                unfilledTaskButton = (TableRow) findViewById(R.id.unfilled_task_button);
                initializeUnfilledTableRow();
                //busy waiting
                updateUnfilledTasksTableRow(areThereUnfinishedTasks());
            }
        };
        uiHandler.post(runnable);

    }

    /**
     * Inflate the main_menu layout
     *
     * @param menu The options menu in which you place your items
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_logout:
                FirebaseAuth.getInstance().signOut();
                if (Profile.getCurrentProfile() != null) {
                    LoginManager.getInstance().logOut(); // log out the facebook button
                }
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.menu_item_settings:
                Intent intent_settings = new Intent(this, SettingsActivity.class);
                intent_settings.putExtra(UserAllOnCompleteListener.CURRENT_USER_KEY, currentUser);
                startActivity(intent_settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method called when add_task_button is clicked.
     * It start a NewTaskActivity with startActivityForResult
     *
     * @param v Required argument
     */
    public void openNewTaskActivity(View v) {
        Intent intent = new Intent(this, NewTaskActivity.class);
        intent.putParcelableArrayListExtra(FilledTaskFragment.TASKS_LIST_KEY, (ArrayList<Task>) FilledTaskFragment.getTaskList());
        startActivityForResult(intent, newTaskRequestCode);
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (newTaskRequestCode == requestCode) {
            if (resultCode == RESULT_OK) {
                // Get result from the result intent.
                Task newTask = data.getParcelableExtra(NewTaskActivity.RETURNED_NEW_TASK);

                //treat the unfilled case
                boolean unfilled = data.getBooleanExtra(TaskActivity.IS_UNFILLED, false);
                if (unfilled) {
                    unfilledTasks.add(newTask);
                    mainFragment.addUnfilled(newTask);

                } else {
                    // Add element to the listTask
                    mainFragment.addTask(newTask);
                }
            }
         } else if(requestCode == unfilledTaskRequestCode) {
                if(resultCode == RESULT_OK){
                    //update the list of unfilledTasks
                    unfilledTasks = data.getParcelableArrayListExtra(UNFILLED_TASKS);
                }

        } else if(requestCode == FilledTaskFragment.EDIT_TASK_REQUEST_CODE){
                mainFragment.onActivityResult(requestCode, resultCode, data);
        }
        updateUnfilledTasksTableRow(areThereUnfinishedTasks());
    }

    /**
     * When the GoogleApiClient is connected it goes here to trigger the geolocation.
     *
     * @param bundle Required argument
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //ask permission to the user.
            // Sufficient to ask just for ACCESS_FINE_LOCATION to have permission for both.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // Get last known recent location:
            Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mCurrentLocation != null) {
                onLocationChanged(mCurrentLocation);
            }
            // Begin polling for new location updates.
            startLocationUpdates();
        }
    }

    /**
     * Called if the connection with the GoogleApiClient is suspended.
     *
     * @param i Required argument
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please reconnect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please reconnect.", Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Called if the connection with the GoogleApiClient failed
     *
     * @param connectionResult Required argument
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    /**
     * Called if the location is changed. If it is, it will change the sort if the location is
     * near one of the user's locations.
     *
     * @param location New location
     */
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        detectedUserLocation = "";

        // calculate the distance between the current location and all the user locations:
        for (ch.epfl.sweng.project.Location userLocation: currentUser.getListLocations()) {
            double distance = haversine(latLng, userLocation);

            if (distance <= 500) { // less than 500 meters
                // set the spinner to the location
                mLocation = (Spinner) findViewById(R.id.location_user);
                mLocation.setSelection(currentUser.getListLocations().indexOf(userLocation));
                detectedUserLocation = userLocation.getName();
            }
        }
    }

    /**
     * Haversine formula calculate the distance in meters between two points by their latitudes
     * and longitudes.
     *
     * Source: http://www.movable-type.co.uk/scripts/latlong.html
     *
     * @param currentLocation current location of the user
     * @param userLocation one of the user's locations
     */
    private double haversine(LatLng currentLocation, ch.epfl.sweng.project.Location userLocation) {
        double dLatitude = Math.toRadians(userLocation.getLatitude()) - Math.toRadians(currentLocation.latitude);
        double dLongitude = Math.toRadians(userLocation.getLongitude()) - Math.toRadians(currentLocation.longitude);

        double a = Math.sin(dLatitude/2) * Math.sin(dLatitude/2) + Math.cos(Math.toRadians(currentLocation.latitude))
                * Math.cos(Math.toRadians(userLocation.getLatitude())) * Math.sin(dLongitude/2)
                * Math.sin(dLongitude/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return 6371000 * c;
    }

    /**
     * start the location update. Fix the priority and the interval of the update.
     */
    private void startLocationUpdates() {
        // Create the location request
        long UPDATE_INTERVAL = 300 * 1000;
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL);
        // Request location updates
        // Problem if it enters with branch
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        initializeAdapters();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void createGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Trigger the dynamic sort.
     */
    public static void triggerDynamicSort() {
        mainFragment.sortTasksDynamically(userLocation, userTimeAtDisposal);
    }

    /**
     * Set the adapter for the images button inside MainActivity layout,
     * so the spinners attach to them dropdown when we click
     * on the image.
     */
    private void initializeAdapters() {
        mDuration = (Spinner) findViewById(R.id.time_user);
        mLocation = (Spinner) findViewById(R.id.location_user);

        String[] locationListForAdapter = getLocationTable();
        for (int i = 0; i < locationListForAdapter.length; i++) {
            if (locationListForAdapter[i].equals(getString(R.string.everywhere_location))) {
                locationListForAdapter[i] = getString(R.string.elsewhere_location);
            }
        }
        locationAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_textview, locationListForAdapter);

        durationAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_textview, getStartDurationTable());

        locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_main);
        durationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_main);

        mLocation.setAdapter(locationAdapter);
        mDuration.setAdapter(durationAdapter);

        //set default value to the spinner
        int spinnerPosition = durationAdapter.getPosition(START_DURATION_MAP.get(userTimeAtDisposal));
        mDuration.setSelection(spinnerPosition);
        mLocation.setSelection(locationAdapter.getPosition(userLocation));

        setListeners(mLocation, mDuration, locationAdapter, durationAdapter);
    }

    private void updateAdapters() {
        mLocation = (Spinner) findViewById(R.id.location_user);

        String[] locationListForAdapter = getLocationTable();
        for (int i = 0; i < locationListForAdapter.length; i++) {
            if (locationListForAdapter[i].equals(getString(R.string.everywhere_location))) {
                locationListForAdapter[i] = getString(R.string.elsewhere_location);
            }
        }

        locationAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_textview, locationListForAdapter);
        locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_main);

        mLocation.setAdapter(locationAdapter);
    }

    /**
     * Set the Listeners in order to have the spinners dropdown when we click
     * on an image button inside the MainActivity layout.
     *
     * @param location        Spinner for the user locations
     * @param duration        Spinner for the time at disposal of the user
     * @param locationAdapter The adapter of location
     * @param durationAdapter The adapter of duration
     */
    private void setListeners(Spinner location, Spinner duration,
                              final ArrayAdapter<String> locationAdapter,
                              final ArrayAdapter<String> durationAdapter) {
        location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (getString(R.string.elsewhere_location).equals(locationAdapter.getItem(position))) {
                    userLocation = getString(R.string.everywhere_location);
                } else {
                    userLocation = locationAdapter.getItem(position);
                }
                if(detectedUserLocation.equals(locationAdapter.getItem(position))) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getColor(R.color.flat_green));
                }

                // trigger the dynamic sort
                triggerDynamicSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        duration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userTimeAtDisposal = REVERSE_START_DURATION.get(durationAdapter.getItem(position));
                // trigger the dynamic sort
                triggerDynamicSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Initialize the functionality of the TableRow
     */
    private void initializeUnfilledTableRow() {
        unfilledTaskButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                GradientDrawable unfilledShape = (GradientDrawable) unfilledTaskButton.getBackground();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        unfilledShape.setColor(Color.argb(255, 255, 255, 255)); // White Tint
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        unfilledShape.setColor(ResourcesCompat.getColor(getResources(), R.color.light_gray, null));
                        Intent intent = new Intent(MainActivity.this, UnfilledTasksActivity.class);
                        intent.putExtra(USER_KEY, getUser());
                        intent.putParcelableArrayListExtra(UNFILLED_TASKS, unfilledTasks);
                        startActivityForResult(intent, unfilledTaskRequestCode);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * Take care of displaying the 4 most recent unfilled task
     * on the tableRow
     */
    private void initializeUnfilledPreview(){
        final int unfilledNbr = unfilledTasks.size();
        GridLayout gridLayout = (GridLayout) findViewById(R.id.unfilled_preview_grid);
        for(int i = 0; i < UNFILLED_TASKS_DIGEST_NBR; ++i){
            int childIndex = ((i<2)?i:((i==2)?3:2));
            TextView unfText = (TextView) gridLayout.getChildAt(childIndex);
            if(i >= unfilledNbr){
                unfText.setVisibility(View.INVISIBLE);
            }else{
                unfText.setVisibility(View.VISIBLE);
                unfText.setText(Utils.separateTitleAndSuffix(unfilledTasks.get(i).getName())[0]);
            }
        }
    }

    /**
     * Set the visibility of the TableRow displaying undone tasks's presence,
     * and update also the number of tasks to be tried.
     *
     * @param visible if true makes it visible, invisible otherwise
     */
    private void updateUnfilledTasksTableRow(boolean visible) {
        if (visible) {
            unfilledTaskButton.setVisibility(View.VISIBLE);
            if (unfilledTasks != null) {

                int taskNum = unfilledTasks.size();
                String numberToDisplay = Integer.toString(taskNum);
                if (taskNum >= 99) {
                    numberToDisplay = "99+";
                }
                TextView taskNumRedDot = (TextView) findViewById(R.id.number_of_unfilled_tasks);
                taskNumRedDot.setText(numberToDisplay);
                initializeUnfilledPreview();
            }
        } else {
            unfilledTaskButton.setVisibility(View.GONE);
        }
    }

    /**
     * Create the maps used by the spinners in MainActivity
     */
    private void createUtilityMaps() {
        DURATION_MAP = new LinkedHashMap<>();
        DURATION_MAP.put(0, mContext.getResources().getString(R.string.select_one));
        DURATION_MAP.put(5, mContext.getResources().getString(R.string.duration5m));
        DURATION_MAP.put(15, mContext.getResources().getString(R.string.duration15m));
        DURATION_MAP.put(30, mContext.getResources().getString(R.string.duration30m));
        DURATION_MAP.put(60, mContext.getResources().getString(R.string.duration1h));
        DURATION_MAP.put(120, mContext.getResources().getString(R.string.duration2h));
        DURATION_MAP.put(240, mContext.getResources().getString(R.string.duration4h));
        DURATION_MAP.put(480, mContext.getResources().getString(R.string.duration1d));
        DURATION_MAP.put(960, mContext.getResources().getString(R.string.duration2d));
        DURATION_MAP.put(1920, mContext.getResources().getString(R.string.duration4d));
        DURATION_MAP.put(2400, mContext.getResources().getString(R.string.duration1w));
        DURATION_MAP.put(4800, mContext.getResources().getString(R.string.duration2w));
        DURATION_MAP.put(9600, mContext.getResources().getString(R.string.duration1m));
        DURATION_MAP = Collections.unmodifiableMap(DURATION_MAP);

        REVERSE_DURATION = new LinkedHashMap<>();
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.select_one), 0);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration5m), 5);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration15m), 15);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration30m), 30);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration1h), 60);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration2h), 120);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration4h), 240);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration1d), 480);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration2d), 960);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration4d), 1920);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration1w), 2400);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration2w), 4800);
        REVERSE_DURATION.put(mContext.getResources().getString(R.string.duration1m), 9600);
        REVERSE_DURATION = Collections.unmodifiableMap(REVERSE_DURATION);

        START_DURATION_MAP = new LinkedHashMap<>();
        START_DURATION_MAP.put(5, mContext.getResources().getString(R.string.duration5m));
        START_DURATION_MAP.put(15, mContext.getResources().getString(R.string.duration15m));
        START_DURATION_MAP.put(30, mContext.getResources().getString(R.string.duration30m));
        START_DURATION_MAP.put(60, mContext.getResources().getString(R.string.duration1h));
        START_DURATION_MAP.put(120, mContext.getResources().getString(R.string.duration2hstartTime));
        START_DURATION_MAP = Collections.unmodifiableMap(START_DURATION_MAP);

        REVERSE_START_DURATION = new LinkedHashMap<>();
        REVERSE_START_DURATION.put(mContext.getResources().getString(R.string.duration5m), 5);
        REVERSE_START_DURATION.put(mContext.getResources().getString(R.string.duration15m), 15);
        REVERSE_START_DURATION.put(mContext.getResources().getString(R.string.duration30m), 30);
        REVERSE_START_DURATION.put(mContext.getResources().getString(R.string.duration1h), 60);
        REVERSE_START_DURATION.put(mContext.getResources().getString(R.string.duration2hstartTime), 120);
        REVERSE_START_DURATION = Collections.unmodifiableMap(REVERSE_START_DURATION);

        ENERGY_MAP = new LinkedHashMap<>();
        ENERGY_MAP.put(0, mContext.getResources().getString(R.string.low_energy));
        ENERGY_MAP.put(1, mContext.getResources().getString(R.string.normal_energy));
        ENERGY_MAP.put(2, mContext.getResources().getString(R.string.high_energy));
        ENERGY_MAP = Collections.unmodifiableMap(ENERGY_MAP);

        REVERSE_ENERGY = new LinkedHashMap<>();
        REVERSE_ENERGY.put(mContext.getResources().getString(R.string.low_energy), 0);
        REVERSE_ENERGY.put(mContext.getResources().getString(R.string.normal_energy), 1);
        REVERSE_ENERGY.put(mContext.getResources().getString(R.string.high_energy), 2);
        REVERSE_ENERGY = Collections.unmodifiableMap(REVERSE_ENERGY);
    }

    /**
     * Construct the table with the favorite locations of the
     * currentUser.
     *
     * @return String[] The array containing the locations of the current user.
     */
    public static String[] getLocationTable() {
        return currentUser.getListNamesLocations().toArray(new String[currentUser.getListLocations().size()]);
    }

    /**
     * Construct the table from which the user can set the time
     * REQUIRED to do a task.
     *
     * @return String[] The array containing the durations.
     */
    public static String[] getDurationTable() {
        return DURATION_MAP.values().toArray(new String[DURATION_MAP.values().size()]);
    }

    /**
     * Construct the table from which the user can set the minimal time
     * REQUIRED before working on the task.
     *
     * @return String[] the array containing the start durations.
     */
    private static String[] getStartDurationTable() {
        return START_DURATION_MAP.values().toArray(new String[START_DURATION_MAP.values().size()]);
    }

    /**
     * Check the validity of the intent
     */
    private void checkIntent() {
        if (intent == null) {
            throw new IllegalArgumentException("No intent was passed to MainActivity");
        }
    }

    /**
     * Check extra passed with the intent
     */
    private void checkIntentExtra() {
        if (currentUser == null/* || taskList == null*/) {
            throw new IllegalArgumentException("User passed with the intent is null");
        }
    }


    /**
     * Tells whether there are unfilled tasks left to be complete
     *
     * @return boolean the existence of unfilled tasks.
     */
    private boolean areThereUnfinishedTasks() {
        return (unfilledTasks != null) && (!unfilledTasks.isEmpty());
    }

    public static void setUser(User updatedUser){
        currentUser = updatedUser;
    }

    public static User getUser(){
        return currentUser;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateAdapters();
        mLocation.setSelection(locationAdapter.getPosition(userLocation));
        mDuration.setSelection(durationAdapter.getPosition(START_DURATION_MAP.get(userTimeAtDisposal)));
        triggerDynamicSort();
    }

    /**
     * Modifies the locations in all the task by replacing the given existing location with the new one
     *
     * @param editedLocation the given existing location
     * @param newLocation the new location
     */
    public static void modifyLocationInTaskList(ch.epfl.sweng.project.Location editedLocation, ch.epfl.sweng.project.Location newLocation) {
        //To avoid concurrent modification
        ArrayList<Task> newTaskList = new ArrayList<>();
        ArrayList<Task> previousTaskList = new ArrayList<>();
        ArrayList<Integer> taskPosition = new ArrayList<>();
        for(int i = 0; i < unfilledTasks.size(); ++i) {
            Task task = unfilledTasks.get(i);
            if (task.getLocationName().equals(editedLocation.getName())) {
                Task previousTask = new Task(task.getName(), task.getDescription(), task.getLocationName(), task.getDueDate(),
                        task.getDurationInMinutes(), task.getEnergy().toString(), task.getListOfContributors(), task.getIfNewContributor(), task.getHasNewMessages());
                Task newTask = new Task(task.getName(), task.getDescription(), newLocation.getName(), task.getDueDate(),
                        task.getDurationInMinutes(), task.getEnergy().toString(), task.getListOfContributors(), task.getIfNewContributor(), task.getHasNewMessages());
                newTaskList.add(newTask);
                previousTaskList.add(previousTask);
                taskPosition.add(i);
            }
        }

        for(int i = 0; i < newTaskList.size(); ++i) {
            FirebaseTaskHelper.updateUnfilledFromMain(currentUser, previousTaskList.get(i), newTaskList.get(i));
        }
    }

    /**
     * Tests whether a location is used by an existing task or not
     *
     * @param locationToCheck the location to check
     * @return true if the location is used, false otherwise
     */
    public static boolean locationIsUsedByTask(ch.epfl.sweng.project.Location locationToCheck) {
        for(Task task : unfilledTasks) {
            if (task.getLocationName().equals(locationToCheck.getName())){
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for the list of unfilled tasks.
     *
     * @return an immutable copy of the unfilledTaskList
     */
    public static List<Task> getUnfilledTaskList() {
        if(unfilledTasks != null){
            return new ArrayList<>(unfilledTasks);
        }else{
            return null;
        }
    }
}
