package ch.epfl.sweng.project;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class which represents an activity regarding a task
 */
public abstract class TaskActivity extends AppCompatActivity {
    Intent intent;
    ArrayList<Task> taskList;
    String[] title;
    String description;
    Long duration;
    String locationName;
    Task.Energy energy;
    List<String> listOfContributors;
    private EditText titleEditText;
    Spinner mLocation;
    private Spinner mDuration;
    private ImageButton doneEditButton;
    static Date date;
    static final DateFormat dateFormat = DateFormat.getDateInstance();
    public static final String IS_UNFILLED = "ch.epfl.sweng.TaskActivity.UNFILLED_TASK";

    private EditText editTextNewContributor;
    TextView contributorsListTextView;

    ImageView editContributorButton;
    private Spinner contributorsSpinner;

    /**
     * Override the onCreate method.
     * Initializes the buttons and fields.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //Initialize the toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.task_toolbar);
        initializeToolbar(mToolbar);

        //Check the validity of the intent
        intent = getIntent();
        checkIntent();
        taskList = intent.getParcelableArrayListExtra(FilledTaskFragment.TASKS_LIST_KEY);
        checkTaskList();

        titleEditText = (EditText) findViewById(R.id.title_task);

        doneEditButton = (ImageButton) findViewById(R.id.edit_done_button_toolbar);

        //Create a listener to check that the user is writing a valid input.
        titleEditText.addTextChangedListener(new TaskTextWatcher());

        mToolbar.setNavigationOnClickListener(new ReturnArrowListener());

        doneEditButton.setOnClickListener(new OnDoneButtonClickListener());

        mLocation = (Spinner) findViewById(R.id.locationSpinner);
        mDuration = (Spinner) findViewById(R.id.durationSpinner);

        ArrayAdapter<String> spinnerDurationAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, MainActivity.getDurationTable());

        mDuration.setAdapter(spinnerDurationAdapter);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_energy);
        radioGroup.check(R.id.energy_normal);

        ArrayAdapter<String> spinnerLocationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.getLocationTable());

        mLocation.setAdapter(spinnerLocationAdapter);
        locationName = Utils.getSelectOne();

        ImageView addContributorButton = (ImageView) findViewById(R.id.addContributorButton);
        addContributorButton.setOnClickListener(new OnAddContributorButtonClickListener());

        editContributorButton = (ImageView) findViewById(R.id.editContributorButton);
        editContributorButton.setOnClickListener(new OnEditContributorButtonClickListener());
    }

    /**
     * Check if the title written is unique or not.
     *
     * @param title The new title of the task
     * @return true if the title is already used or false otherwise.
     */
    abstract boolean titleIsNotUnique(String title);

    abstract void resultActivity();

    ImageButton getDoneEditButton() {
        return doneEditButton;
    }

    /**
     * A user shouldn't be allowed to type "@@", which
     * is the dedicated separator put in the title to signal
     * the creator of a task, and the person with which the task
     * is shared.
     *
     * @param title the new title of the task
     * @return true if the title contains
     */
    private boolean titleContainsContributorsSeparators(String title){
        char lastChar = title.charAt(title.length() -1);
        return title.contains(getString(R.string.contributors_separator))
                || lastChar == getString(R.string.contributors_separator).charAt(0);
    }

    /**
     * Check that the title does not contain '.', '#', '$', '[', or ']'
     */
    private boolean titleIsCompatibleWithFirebase(String title) {
        String[] invalidChars = new String[]{".", "#", "$", "[", "]"};
        for(String s : invalidChars) {
            if(title.contains(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check that the intent is valid
     */
    private void checkIntent() {
        if (intent == null) {
            throw new IllegalArgumentException("No intent was passed to TaskActivity !");

        }
    }

    /**
     * Method that checks that the task's list is not null
     */
    private void checkTaskList() {
        if (taskList == null) {
            throw new IllegalArgumentException("Error on taskList passed with the intent");
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }
    }

    /**
     * Private class that implement TextWatcher.
     * This class is used to check on runtime if the inputs written by the user
     * are valid or not.
     */
    private class TaskTextWatcher implements TextWatcher {

        /**
         * Check the input written by the user before it is changed.
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        /**
         * Check the input written by the user after it was changed.
         */
        @Override
        public void afterTextChanged(Editable s) {
            if (titleIsNotUnique(s.toString())) {
                doneEditButton.setVisibility(View.INVISIBLE);
                titleEditText.setError(getResources().getText(R.string.error_title_duplicated));
            } else if(!titleIsCompatibleWithFirebase(s.toString())) {
                doneEditButton.setVisibility(View.INVISIBLE);
                titleEditText.setError(getResources().getText(R.string.error_valid_title_firebase));
            } else if (s.toString().isEmpty()) {
                doneEditButton.setVisibility(View.INVISIBLE);
                titleEditText.setError(getResources().getText(R.string.error_title_empty));
            } else if(titleContainsContributorsSeparators(s.toString())) {
                doneEditButton.setVisibility(View.INVISIBLE);
                String errorAtAt =getResources().getText(R.string.error_title_contains_contributors_separator).toString()
                        + getResources().getText(R.string.contributors_separator).toString();
                titleEditText.setError(errorAtAt);
            } else {
                doneEditButton.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * Listener of the done button
     */
    private class OnDoneButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //need this null check on the case of NewTaskActivity
            if(title == null){
                title = new String[2];
            }
            title[0] = titleEditText.getText().toString();
            if (title[0].isEmpty()) {
                titleEditText.setError(getResources().getText(R.string.error_title_empty));
            } else if (!title[0].isEmpty() && !titleIsNotUnique(title[0])) {
                EditText descriptionEditText = (EditText) findViewById(R.id.description_task);
                description = descriptionEditText.getText().toString();
                if(mLocation.getSelectedItem() != null) {
                    locationName = mLocation.getSelectedItem().toString();
                }
                if(mDuration.getSelectedItem() != null) {
                    duration = MainActivity.REVERSE_DURATION
                            .get(mDuration.getSelectedItem().toString()).longValue();
                }
                Log.e("taskActivity", "duration is : " + duration);
                // to set correctly the energy from the radio button
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_energy);
                int index = radioGroup.indexOfChild(findViewById(radioGroup.getCheckedRadioButtonId()));
                switch(index) {
                    case 0:
                        energy = Task.Energy.LOW;
                        break;
                    case 1:
                        energy = Task.Energy.NORMAL;
                        break;
                    case 2:
                        energy = Task.Energy.HIGH;
                        break;
                    default:
                        energy = Task.Energy.NORMAL;
                        break;
                }

                resultActivity();
            }
        }
    }

    /**
     * Class that implements OnClickListener.
     * It represents a OnClickListener on the return arrow.
     */
    private class ReturnArrowListener implements View.OnClickListener {

        /**
         * Called when the return arrow has been clicked.
         *
         * @param v The view that was clicked, the return arrow.
         */
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    /*
     * Method to hide keyboard when clicking outside the EditTextView
     *
     * source : http://stackoverflow.com/questions/4828636/edittext-clear-focus-on-touch-outside
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    /**
     * Method displaying the date picker dialog
     */
    public void showDatePickerDialog(View  v) {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    /**
     * Method that assign value to energy when user checks an energy radio button
     *
     * @param view The selected radio button
     */
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.energy_low:
                if (checked)
                    energy = Task.Energy.LOW;
                break;
            case R.id.energy_normal:
                if (checked)
                    energy = Task.Energy.NORMAL;
                break;
            case R.id.energy_high:
                if (checked)
                    energy = Task.Energy.HIGH;
                break;
        }
    }

    /**
     * Class representing the dialog to pick the date of a task
     */
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        Button mButton;

        @NonNull
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mButton = (Button) getActivity().findViewById(R.id.pick_date);
            final Calendar c = Calendar.getInstance();
            int year;
            int month;
            int day;

            if(mButton.getText().equals(getString(R.string.enter_due_date_hint))
                    || date.getTime() == NewTaskActivity.UNFILLED_TASK_TIME){
                // Use the current date as the default date in the picker
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            } else {
                c.setTime(date);
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void onDateSet(DatePicker view, int year, int month, int day) {
            mButton = (Button) getActivity().findViewById(R.id.pick_date);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            date = cal.getTime();
            mButton.setText(dateFormat.format(date.getTime()));
        }

    }

    /**
     * Display the contributors
     */
    void setContributorsTextView(){
        String listOfContributorsString = "";
        for(String ct : listOfContributors) {
            listOfContributorsString += (ct + "\n");
        }
        contributorsListTextView.setText(listOfContributorsString);
        contributorsListTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Add a new contributor to the task
     *
     * @param contributor the new contributor to add
     */
    abstract void addContributorInTask(String contributor);

    /**
     * Removes a given contributor from the task
     *
     * @param contributor the contributor to remove
     */
    abstract void deleteContributorInTask(String contributor);

    /**
     * Listener that trigger the contributors addition when clicking on the button
     */
    private class OnAddContributorButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setMessage(R.string.contributor_dialog_message).setTitle(R.string.contributor_dialog_title);

            //EditText for new contributor
            editTextNewContributor = new EditText(getApplicationContext());
            editTextNewContributor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            editTextNewContributor.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            builder.setView(editTextNewContributor);

            //Show dialog
            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final String enteredText = editTextNewContributor.getText().toString();

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("users").child(Utils.encodeMailAsFirebaseKey(enteredText)).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()){
                                        dialog.setMessage(getString(R.string.contributor_dialog_warning_message));
                                    } else if (listOfContributors.contains(enteredText)){
                                        dialog.setMessage(getString(R.string.contributor_dialog_duplicate_warning_message));
                                    } else{
                                        addContributorInTask(enteredText);
                                        if(MainActivity.getUser().getEmail().equals(listOfContributors.get(0))) {
                                            editContributorButton.setVisibility(View.VISIBLE);
                                        }
                                        setContributorsTextView();
                                        dialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );
                }
            });
        }
    }

    /**
     * Listener that triggers the contributor editor when clicking on the button
     */
    private class OnEditContributorButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String contributorToDelete = contributorsSpinner.getSelectedItem().toString();
                    deleteContributorInTask(contributorToDelete);
                    setContributorsTextView();
                    if (listOfContributors.size() == 1) {
                        editContributorButton.setVisibility(View.GONE);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setMessage(R.string.delete_contributor_dialog_message).setTitle(R.string.delete_contributor_dialog_title);

            //Spinner
            ArrayList<String> listOfContributorsForSpinner = new ArrayList<>(listOfContributors);
            listOfContributorsForSpinner.remove(0);
            String[] spinnerList = listOfContributorsForSpinner.toArray(new String[listOfContributorsForSpinner.size()]);
            final ArrayAdapter<String> adp = new ArrayAdapter<>(getApplicationContext(),
                    R.layout.dialog_spinner_item, spinnerList);
            adp.setDropDownViewResource(R.layout.dialog_spinner_dropdown_item);

            contributorsSpinner = new Spinner(getApplicationContext());
            contributorsSpinner.setAdapter(adp);
            contributorsSpinner.setPadding(50, 0 , 50, 0);
            builder.setView(contributorsSpinner);

            //Show dialog
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
