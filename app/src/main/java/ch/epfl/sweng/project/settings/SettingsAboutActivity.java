package ch.epfl.sweng.project.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import ch.epfl.sweng.project.R;

/**
 * Class that provides the information about
 * the developers that participated in the creation of the app.
 */
public class SettingsAboutActivity extends AppCompatActivity {

    /**
     * Override the onCreate method
     * Initializes the fields
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about);

        setTeamNames();

        setAppVersion();

    }

    /**
     * Get the emoji corresponding to the unicode
     *
     * @param unicode hexadecimal representing an emoji
     * @return the emoji as a String
     */
    private String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    /**
     * Get the App version and write it in the corresponding TextView
     */
    private void setAppVersion(){
        TextView version = (TextView) findViewById(R.id.settings_about_text_version);
        try {
            PackageInfo _info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(_info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version.setText("");
        }
    }

    /**
     * Set the team name for each corresponding TextView
     */
    private void setTeamNames() {
        TextView nameMikael = (TextView) findViewById(R.id.settings_about_text_mikael);
        TextView nameCharles = (TextView) findViewById(R.id.settings_about_text_charles);
        TextView nameMatteo = (TextView) findViewById(R.id.settings_about_text_matteo);
        TextView nameCedric = (TextView) findViewById(R.id.settings_about_text_cedric);
        TextView nameBastian = (TextView) findViewById(R.id.settings_about_text_bastian);
        TextView nameIlkan = (TextView) findViewById(R.id.settings_about_text_ilkan);

        nameMikael.setText(String.format("%s%s", getEmojiByUnicode(0x1F680), getString(R.string.settings_about_mikael)));
        nameCharles.setText(String.format("%s%s", getEmojiByUnicode(0x1F682), getString(R.string.settings_about_charles)));
        nameMatteo.setText(String.format("%s%s", getEmojiByUnicode(0x1F34E), getString(R.string.settings_about_matteo)));
        nameCedric.setText(String.format("%s%s", getEmojiByUnicode(0x1F3B8), getString(R.string.settings_about_cedric)));
        nameBastian.setText(String.format("%s%s", getEmojiByUnicode(0x1F355), getString(R.string.settings_about_bastian)));
        nameIlkan.setText(String.format("%s%s", getEmojiByUnicode(0x1F6F0), getString(R.string.settings_about_ilkan)));
    }
}
