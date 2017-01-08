package ch.epfl.sweng.project.location_setting;

import android.os.Bundle;
import android.widget.EditText;

import ch.epfl.sweng.project.Location;
import ch.epfl.sweng.project.R;

/**
 * Class that represents the inflated activity_location_settings under the edit case
 */
public class EditLocationActivity extends LocationActivity {
    public static final String RETURNED_EDITED_LOCATION = "ch.epfl.sweng.EditLocationActivity.EDITED_LOCATION";
    public static final String RETURNED_INDEX_EDITED_LOCATION = "ch.epfl.sweng.EditLocationActivity.RETURNED_INDEX_EDITED_LOCATION";
    private Location mLocationToBeEdited;
    private int mIndexLocationToBeEdited;

    /**
     * Override the onCreate method
     * Recover the location to be edited and update it, then it puts the
     * edited location in the intent.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it
     *                           most recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the index and check its validity
        mIndexLocationToBeEdited = intent.getIntExtra(LocationFragment.INDEX_LOCATION_TO_BE_EDITED_KEY, -1);
        checkLocationToBeEditedIndex();

        //Get the location to be edited
        mLocationToBeEdited = locationList.get(mIndexLocationToBeEdited);

        //Populate the layout activity_location_settings
        populateLayout();
    }

    /**
     * Check if the location name written is unique or not.
     *
     * @param name The new name of the location
     * @return true if the name is already used or false otherwise.
     */
    @Override
    boolean nameIsNotUnique(String name) {
        boolean result = false;
        for (int i = 0; i < locationList.size(); i++) {
            if (locationList.get(i).getName().equals(name) && i != mIndexLocationToBeEdited) {
                result = true;
            }
        }
        return result;
    }

    @Override
    protected void resultActivity() {
        mLocationToBeEdited.setName(name);
        mLocationToBeEdited.setLatitude(latitude);
        mLocationToBeEdited.setLongitude(longitude);

        intent.putExtra(RETURNED_EDITED_LOCATION, mLocationToBeEdited);
        intent.putExtra(RETURNED_INDEX_EDITED_LOCATION, mIndexLocationToBeEdited);
    }

    /**
     * Check that the 'location to be edited' 's index is valid
     *
     * @throws IllegalArgumentException If there is an error with the intent passed
     *                                  to the activity.
     */
    private void checkLocationToBeEditedIndex() {
        if (mIndexLocationToBeEdited == -1) {
            throw new IllegalArgumentException("Error on the index passed with the intent !");
        }
    }

    /**
     * Fill the layout with the old values of the location to be edited.
     */
    private void populateLayout() {
        EditText titleEditText = (EditText) findViewById(R.id.locationName);
        titleEditText.setText(mLocationToBeEdited.getName());
        titleEditText.setSelection(titleEditText.getText().length());
        latitude = mLocationToBeEdited.getLatitude();
        longitude = mLocationToBeEdited.getLongitude();
    }
}
