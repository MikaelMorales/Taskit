package ch.epfl.sweng.project.location_setting;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.project.FilledTaskFragment;
import ch.epfl.sweng.project.Location;
import ch.epfl.sweng.project.MainActivity;
import ch.epfl.sweng.project.R;
import ch.epfl.sweng.project.User;
import ch.epfl.sweng.project.data.FirebaseUserHelper;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Class that represents the inflated fragment located in the activity_main
 */
public class LocationFragment extends Fragment {
    public static final String INDEX_LOCATION_TO_BE_EDITED_KEY = "ch.epfl.sweng.LocationFragment._INDEX_LOCATION_TO_BE_EDITED";
    public static final String LOCATIONS_LIST_KEY = "ch.epfl.sweng.LocationFragment.LOCATIONS_LIST";
    private final int editLocationRequestCode = 2;
    private LocationListAdapter mLocationAdapter;
    private LocationListAdapter mDefaultLocationAdapter;
    private ArrayList<Location> locationList;
    private ArrayList<Location> defaultLocationList;
    private static User currentUser;
    private boolean firstConnection;

    private Spinner locationSpinnerForReplacement;

    private ListView listView;

    private final int ITEM_HEIGHT = 180;

    /**
     * Override the onCreate method. It initialize the database, the list of location
     * and the custom made adapter.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationList = new ArrayList<>();
        defaultLocationList = new ArrayList<>();

        mLocationAdapter = new LocationListAdapter(
                getActivity(),
                R.layout.list_item_location,
                locationList
        );

        mDefaultLocationAdapter = new LocationListAdapter(
                getActivity(),
                R.layout.list_item_location,
                defaultLocationList
        );

        SharedPreferences prefs = getContext().getSharedPreferences(getString(R.string.application_prefs_name), MODE_PRIVATE);
        firstConnection = prefs.contains(getString(R.string.new_user))
                && prefs.getBoolean(getString(R.string.new_user), true);

        if (prefs.getBoolean(getString(R.string.new_user), true)) {
            addDefaultLocations();
        } else if (!firstConnection) {
            //If accessed from settings, load default locations
            addDefaultLocation(new Location(getString(R.string.everywhere_location), 0, 0));
            addDefaultLocation(new Location(getString(R.string.downtown_location), 0, 0));

            Bundle bundle = this.getArguments();
            if (bundle != null) {
                currentUser = bundle.getParcelable(LocationSettingActivity.USER_KEY);
                if (currentUser == null) {
                    throw new IllegalArgumentException("User passed with the intend is null");
                } else {
                    List<Location> listOfCurrentUserLocations = currentUser.getListLocations();
                    for(int i = 3; i < listOfCurrentUserLocations.size(); ++i){
                        addLocation(listOfCurrentUserLocations.get(i));
                    }
                }
            } else {
                throw new NullPointerException("User was badly passed from LocationSettingsActivity to LocationFragment !");
            }
        }
    }

    /**
     * Override the onCreateView method to initialize the adapter of
     * the ListView.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment
     * @param container          Parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here
     * @return the view of the list to be displayed
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_location_list, container, false);

        listView = (ListView) rootView.findViewById(R.id.list_view_locations);
        listView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ITEM_HEIGHT * mLocationAdapter.getCount()));
        listView.setAdapter(mLocationAdapter);

        ListView listViewDefault = (ListView) rootView.findViewById(R.id.default_list_view_locations);
        listViewDefault.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ITEM_HEIGHT * mDefaultLocationAdapter.getCount()));
        listViewDefault.setAdapter(mDefaultLocationAdapter);

        registerForContextMenu(listView);
        //registerForContextMenu(listViewDefault);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EditLocationActivity.class);
                intent.putExtra(INDEX_LOCATION_TO_BE_EDITED_KEY, position);
                intent.putParcelableArrayListExtra(LOCATIONS_LIST_KEY, locationList);
                startActivityForResult(intent, editLocationRequestCode);
            }
        });

        listViewDefault.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), R.string.info_cant_edit_delete, Toast.LENGTH_LONG).show();
            }
        });

        return rootView;
    }

    /**
     * Override the onCreateContextMenu method.
     * This method creates a floating context menu.
     *
     * @param menu     The context menu that is being built.
     * @param v        The view for which the context menu is being built.
     * @param menuInfo Extra information about the item
     *                 for which the context menu should be shown
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.floating_location_menu, menu);
    }

    /**
     * Override the onContextItemSelected.
     * This method decides what to do depending of the context menu's item
     * selected by the user.
     *
     * @param item The context menu item that was selected
     * @return Return false to allow normal context menu processing to proceed,
     * true to consume it here
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.floating_location_delete:
                if (!firstConnection) {
                    if (FilledTaskFragment.locationIsUsedByTask(locationList.get(itemInfo.position))
                            || MainActivity.locationIsUsedByTask(locationList.get(itemInfo.position))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        // Add the buttons
                        builder.setPositiveButton(R.string.ok, new MyOnClickListener(itemInfo));
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        builder.setMessage(R.string.location_delete_dialog_message).setTitle(R.string.location_delete_dialog_title);
                        //Create spinner
                        ArrayList<String> listOfLocationForSpinner = currentUser.getListNamesLocations();
                        if(listOfLocationForSpinner.contains(locationList.get(itemInfo.position).getName())) {
                            listOfLocationForSpinner.remove(itemInfo.position + 3);
                        }
                        String[] spinnerList = listOfLocationForSpinner.toArray(new String[listOfLocationForSpinner.size()]);
                        for (int i = 0; i < spinnerList.length; i++) {
                            if (spinnerList[i].equals(getString(R.string.select_one))) {
                                spinnerList[i] = getString(R.string.unfilled_param);
                            }
                        }

                        final ArrayAdapter<String> adp = new ArrayAdapter<>(getApplicationContext(),
                                R.layout.dialog_spinner_item, spinnerList);
                        adp.setDropDownViewResource(R.layout.dialog_spinner_dropdown_item);
                        locationSpinnerForReplacement = new Spinner(getApplicationContext());

                        locationSpinnerForReplacement.setAdapter(adp);
                        locationSpinnerForReplacement.setPadding(100, 50, 100, 50);

                        builder.setView(locationSpinnerForReplacement);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        removeLocation(itemInfo);
                        listView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ITEM_HEIGHT * mLocationAdapter.getCount()));
                        currentUser = new User(currentUser.getEmail(), getLocationList());
                        FirebaseUserHelper.updateUser(currentUser);
                        MainActivity.setUser(currentUser);
                    }
                } else {
                    removeLocation(itemInfo);
                    listView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ITEM_HEIGHT * mLocationAdapter.getCount()));
                }
                return true;
            case R.id.floating_location_edit:
                startEditLocationActivity(itemInfo);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Method called when an activity launch inside LocationSettingActivity,
     * is finished. This method is triggered only if we use
     * startActivityForResult.
     *
     * @param requestCode The integer request code supplied to startActivityForResult
     *                    used as an identifier.
     * @param resultCode  The integer result code returned by the child activity
     * @param data        An intent which can return result data to the caller.
     * @throws IllegalArgumentException if the returned extras from EditLocationActivity are
     *                                  invalid
     * @throws SQLiteException          if more that one row was changed when editing a location.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Case when we returned from the EditLocationActivity
        if (requestCode == editLocationRequestCode && resultCode == RESULT_OK) {
            actionOnActivityResult(data);
        }
    }

    /**
     * Private method called by onActivityResult to change a location of the task.
     *
     * @param data The intent returned to onActivityResult
     */
    private void actionOnActivityResult(Intent data) {
        // Get result from the result intent.
        Location editedLocation = data.getParcelableExtra(EditLocationActivity.RETURNED_EDITED_LOCATION);
        int indexEditedLocation = data.getIntExtra(EditLocationActivity.RETURNED_INDEX_EDITED_LOCATION, -1);
        if (indexEditedLocation == -1 || editedLocation == null) {
            throw new IllegalArgumentException("Invalid extras returned from EditLocationActivity !");
        } else {
            if (!firstConnection) {
                if (FilledTaskFragment.locationIsUsedByTask(locationList.get(indexEditedLocation))
                        || MainActivity.locationIsUsedByTask(locationList.get(indexEditedLocation))) {
                    //Replace location edited in all tasks using it
                    FilledTaskFragment.modifyLocationInTaskList(locationList.get(indexEditedLocation), editedLocation);
                    MainActivity.modifyLocationInTaskList(locationList.get(indexEditedLocation), editedLocation);
                }
            }
            locationList.set(indexEditedLocation, editedLocation);
            mLocationAdapter.notifyDataSetChanged();
            String toast = editedLocation.getName() + getString(R.string.info_updated);
            Toast.makeText(getActivity().getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            if (!firstConnection) {
                currentUser = new User(currentUser.getEmail(), getLocationList());
                FirebaseUserHelper.updateUser(currentUser);
                MainActivity.setUser(currentUser);
            }
        }
    }

    /**
     * Start the EditLocationActivity for result when the user press the edit button.
     * The location index and the locationList are passed as extras to the intent.
     *
     * @param itemInfo Extra information about the item
     *                 for which the context menu should be shown
     */
    private void startEditLocationActivity(AdapterView.AdapterContextMenuInfo itemInfo) {
        int position = itemInfo.position;
        Intent intent = new Intent(getActivity(), EditLocationActivity.class);

        intent.putExtra(INDEX_LOCATION_TO_BE_EDITED_KEY, position);
        intent.putParcelableArrayListExtra(LOCATIONS_LIST_KEY, locationList);

        startActivityForResult(intent, editLocationRequestCode);
    }

    /**
     * Method that adds a location in the locationList and in the database.
     *
     * @param location The location to be added
     * @throws IllegalArgumentException If the location to be added is null
     */
    public void addLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException();
        }
        locationList.add(location);
        mLocationAdapter.notifyDataSetChanged();

        if(!firstConnection){
            currentUser = new User(currentUser.getEmail(), getLocationList());
            FirebaseUserHelper.updateUser(currentUser);
        }
    }

    /**
     * Add a default location to the list of locations
     *
     * @param location The location
     */
    private void addDefaultLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException();
        }

        defaultLocationList.add(location);
        mDefaultLocationAdapter.notifyDataSetChanged();
    }

    /**
     * Add several default locations to the list
     */
    private void addDefaultLocations() {
        addDefaultLocation(new Location(getString(R.string.everywhere_location), 0, 0));
        addDefaultLocation(new Location(getString(R.string.downtown_location), 0, 0));

        addLocation(new Location(getString(R.string.home_location), 0, 0));
        addLocation(new Location(getString(R.string.office_location), 0, 0));
        addLocation(new Location(getString(R.string.school_location), 0, 0));
    }

    /**
     * Remove a location and the locationList.
     *
     * @param itemInfo Extra information about the item
     *                 for which the context menu should be shown
     * @throws SQLiteException if an error occurred
     */
    private void removeLocation(AdapterView.AdapterContextMenuInfo itemInfo) {
        int position = itemInfo.position;
        removeLocationAction(position);

    }

    /**
     * Remove the location from the user
     */
    private void removeLocationAction(int position) {
        Location locationToBeDeleted = locationList.get(position);
        String locationName = locationToBeDeleted.getName();

        locationList.remove(locationToBeDeleted);
        mLocationAdapter.notifyDataSetChanged();

        Context context = getActivity().getApplicationContext();
        String TOAST_MESSAGE = locationName + getString(R.string.info_deleted);
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, TOAST_MESSAGE, duration).show();
    }

    /**
     * Getter for the locationList
     *
     * @return an immutable copy of locationList
     */
    public List<Location> getLocationList() {
        ArrayList<Location> tmp = new ArrayList<>(defaultLocationList);
        tmp.add(0, new Location(getString(R.string.select_one), 0, 0));
        tmp.addAll(locationList);
        return tmp;
    }

    /**
     * Listener of the button that select a location
     */
    public class MyOnClickListener implements DialogInterface.OnClickListener {

        final AdapterView.AdapterContextMenuInfo itemInfo;

        MyOnClickListener(AdapterView.AdapterContextMenuInfo itemInfo) {
            this.itemInfo = itemInfo;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String newLocationName = locationSpinnerForReplacement.getSelectedItem().toString();
            if (getString(R.string.unfilled_param).equals(newLocationName)) {
                newLocationName = getString(R.string.select_one);
            }

            //Replace locations
            //Create a location from which coordinates don't matter because only the title is stored in the tasks
            FilledTaskFragment.modifyLocationInTaskList(locationList.get(itemInfo.position), new Location(newLocationName, 0, 0));
            MainActivity.modifyLocationInTaskList(locationList.get(itemInfo.position), new Location(newLocationName, 0, 0));

            removeLocation(itemInfo);
            listView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ITEM_HEIGHT * mLocationAdapter.getCount()));
            currentUser = new User(currentUser.getEmail(), getLocationList());
            FirebaseUserHelper.updateUser(currentUser);
            MainActivity.setUser(currentUser);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        listView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ITEM_HEIGHT * mLocationAdapter.getCount() + 1));
    }
}
