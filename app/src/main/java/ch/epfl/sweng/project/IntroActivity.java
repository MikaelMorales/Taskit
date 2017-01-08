package ch.epfl.sweng.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import ch.epfl.sweng.project.settings.SettingsActivity;

/**
 * Tutorial activity that explains basic concepts of the app
 * to the users.
 */
public class IntroActivity extends AppIntro {

    /**
     * Override the onCreate method
     * Initializes the buttons and fields
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        addSlide(AppIntroFragment.newInstance(getString(R.string.tutorial_slide_1_title), getString(R.string.tutorial_slide_1_description), R.drawable.logo_white_middle_det, getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.tutorial_slide_2_title), getString(R.string.tutorial_slide_2_description), R.drawable.automatic_sort_illustration, getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.tutorial_slide_3_title), getString(R.string.tutorial_slide_3_description), R.drawable.params_illustration, getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.tutorial_slide_4_title), getString(R.string.tutorial_slide_4_description), R.drawable.date_illustration, getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.tutorial_slide_5_title), getString(R.string.tutorial_slide_5_description), R.drawable.location_illustration, getColor(R.color.colorPrimary)));

        setDoneText(getString(R.string.done_button));
        setSkipText(getString(R.string.skip_button));
    }

    /**
     * Got to the next activity (selected in goToNextActivity())
     *
     * @param currentFragment the current fragment created in on create
     */
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        goToNextActivity();
    }

    /**
     * Got to the next activity (selected in goToNextActivity())
     *
     * @param currentFragment the current fragment created in on create
     */
    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        goToNextActivity();
    }

    /**
     * Select the next activity :
     * - LoginActivity if the user opens the app for the first time
     * - SettingsActivity if the user accessed the tutorial from the settings
     */
    private void goToNextActivity() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.application_prefs_name), MODE_PRIVATE);

        // for the special case when the tuto is open at the first utilisation
        if(prefs.getBoolean(getString(R.string.first_launch), true)){
            prefs.edit().putBoolean(getString(R.string.first_launch), false).apply();
            Intent intent = new Intent(IntroActivity.this, EntryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else // if not then we return to the settings
        {
            Intent intent = new Intent(IntroActivity.this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}