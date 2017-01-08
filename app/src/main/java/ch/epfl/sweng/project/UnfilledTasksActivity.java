package ch.epfl.sweng.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

/**
 * Activity with the task that are unfilled
 */
public class UnfilledTasksActivity extends AppCompatActivity {

    private UnfilledTaskFragment unfilledFragment;

    /**
     * Override the onCreate method
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_unfilled_tasks);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.unfilled_tasks_toolbar);
        initializeToolbar(mToolbar);
        mToolbar.setNavigationOnClickListener(new OnReturnArrowClickListener());

        ArrayList<Task> unfilledTasks = getIntent().getParcelableArrayListExtra(MainActivity.UNFILLED_TASKS);
        User currUser = getIntent().getParcelableExtra(MainActivity.USER_KEY);

        //Add the user to TaskFragments
        unfilledFragment = new UnfilledTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MainActivity.UNFILLED_TASKS, unfilledTasks);
        bundle.putParcelable(MainActivity.USER_KEY, currUser);
        unfilledFragment.setArguments(bundle);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.unfilled_tasks_container, unfilledFragment)
                    .commit();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        unfilledFragment.onActivityResult(requestCode,resultCode,data);
    }

    /**
     * OnClickListener on the return arrow.
     */
    private class OnReturnArrowClickListener implements View.OnClickListener {

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            setResultIntent();
            finish();
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        setResultIntent();
        finish();
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
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    /**
     * Private method that set the resulting intent
     */
    private void setResultIntent() {
        Intent intent = getIntent();
        intent.putParcelableArrayListExtra(MainActivity.UNFILLED_TASKS,(ArrayList<Task>) unfilledFragment.getUnfilledTaskList());
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (unfilledFragment.getUnfilledTaskList().size() == 0) {
            onBackPressed();
        }
    }
}
