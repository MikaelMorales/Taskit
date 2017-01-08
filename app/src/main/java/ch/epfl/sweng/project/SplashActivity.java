package ch.epfl.sweng.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

/**
 * Activity shown at the launch of the app to avoid
 * having a white screen.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Override the onCreate method
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, EntryActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
}
