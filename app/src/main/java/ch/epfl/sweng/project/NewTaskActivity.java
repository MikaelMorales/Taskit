package ch.epfl.sweng.project;

import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Class that represents the inflated activity_new_task
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class NewTaskActivity extends TaskActivity {
    public static final String RETURNED_NEW_TASK = "ch.epfl.sweng.NewTaskActivity.NEW_TASK";
    public static final long UNFILLED_TASK_TIME = 0;

    /**
     * Override the onCreate method
     * Initializes the buttons and fields
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove chat button
        FloatingActionButton chatButton = (FloatingActionButton) findViewById(R.id.open_chat);
        chatButton.setVisibility(View.INVISIBLE);

        //Set default values
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(UNFILLED_TASK_TIME);
        date = cal.getTime();

        energy = Task.Energy.NORMAL;

        //prepare contributors
        listOfContributors = new ArrayList<>();
        String contributor;

        try {
            contributor = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } catch (NullPointerException e) {
            contributor = User.DEFAULT_EMAIL;
        }

        listOfContributors.add(contributor);
        editContributorButton.setVisibility(View.GONE);

        contributorsListTextView = (TextView) findViewById(R.id.text_contributors);
        setContributorsTextView();

        getEditableView();
    }

    /**
     * Add a new contributor to the task
     *
     * @param contributor the new contributor to add
     */
    @Override
    void addContributorInTask(String contributor){
        listOfContributors.add(contributor);
        if(!locationName.equals(Utils.getEverywhereLocation()) && listOfContributors.size() == 2) {
            Toast.makeText(getApplicationContext(), R.string.location_warning_if_multiple_contributors, Toast.LENGTH_LONG).show();
        }
        locationName = Utils.getEverywhereLocation();
        mLocation.setSelection(1);
        ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.switcher_location);
        switcher.showNext();
        switcher.setEnabled(false);
        TextView locationTextView = (TextView) findViewById(R.id.text_location);
        locationTextView.setText(Utils.getEverywhereLocation());
    }

    /**
     * Removes a given contributor from the task
     *
     * @param contributor the contributor to remove
     */
    @Override
    void deleteContributorInTask(String contributor) {
        listOfContributors.remove(contributor);
        if (listOfContributors.size() == 1) {
            ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.switcher_location);
            switcher.showNext();
            switcher.setEnabled(true);
        }
    }

    /**
     * Check if the title written is unique or not.
     *
     * @param title The new title of the task
     * @return true if the title is already used or false otherwise.
     */
    @Override
    boolean titleIsNotUnique(String title) {
        boolean result = false;
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.addAll(MainActivity.getUnfilledTaskList());
        allTasks.addAll(taskList);
        for (Task task : allTasks) {
            if (task.getName().equals(title)) {
                result = true;
            }
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    void resultActivity() {
        String titleToType;
        if(listOfContributors.size() > 1){
            String creatorEmail = listOfContributors.get(0);
            titleToType = Utils.constructSharedTitle(title[0], creatorEmail, creatorEmail);
        }else{
            titleToType = title[0];
        }
        Task newTask = new Task(titleToType, description, locationName, date, duration, energy.toString(), listOfContributors, 0L, false);
        intent.putExtra(RETURNED_NEW_TASK, newTask);

        if(Utils.isUnfilled(newTask)){
            intent.putExtra(IS_UNFILLED, true);
        }else{
            intent.putExtra(IS_UNFILLED, false);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Switch the TextView to editable View
     * (when we create a new task it's useless to displayed the task's parameters because there are unfilled)
     */
    private void getEditableView() {
        ((ViewSwitcher) findViewById(R.id.switcher_name)).showNext();
        ((ViewSwitcher) findViewById(R.id.switcher_description)).showNext();
        ((ViewSwitcher) findViewById(R.id.switcher_energy)).showNext();
        ((ViewSwitcher) findViewById(R.id.switcher_location)).showNext();
        ((ViewSwitcher) findViewById(R.id.switcher_duration)).showNext();
        ((ViewSwitcher) findViewById(R.id.switcher_date)).showNext();
    }
}
