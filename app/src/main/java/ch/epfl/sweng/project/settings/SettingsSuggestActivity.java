package ch.epfl.sweng.project.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.sweng.project.R;

/**
 * Activity where users can suggest improvements for the app by
 * sending an email to a developer.
 *
 */
public class SettingsSuggestActivity extends AppCompatActivity {

    private TextInputLayout textInputLayoutName;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutMessage;

    /**
     * Override the onCreate method
     * Initializes the buttons and fields
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_suggest);

        textInputLayoutName = (TextInputLayout) findViewById(R.id.settings_suggest_textinputlayout_name);
        textInputLayoutEmail = (TextInputLayout) findViewById(R.id.settings_suggest_textinputlayout_email);
        textInputLayoutMessage = (TextInputLayout) findViewById(R.id.settings_suggest_textinputlayout_message);

        sendEmail();

    }

    /**
     * Construct and send the mail to nanchenbastian@gmail.com
     */
    private void sendEmail() {
        Button sendButton = (Button) findViewById(R.id.settings_suggest_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.settings_suggest_name);
                EditText mail = (EditText) findViewById(R.id.settings_suggest_mail);
                EditText message = (EditText) findViewById(R.id.settings_suggest_message);

                // Control if all the fields are filled and if the email address has a correct format
                if (name.getText().toString().trim().equals("")) {
                    textInputLayoutName.setErrorEnabled(true);
                    textInputLayoutName.setError(getString(R.string.settings_suggest_field_mandatory));
                    return;
                } else {
                    textInputLayoutName.setErrorEnabled(false);
                }

                if (mail.getText().toString().trim().equals("")) {
                    textInputLayoutEmail.setErrorEnabled(true);
                    textInputLayoutEmail.setError(getString(R.string.settings_suggest_field_mandatory));
                    return;
                } else {
                    textInputLayoutEmail.setErrorEnabled(false);
                }

                if (!isEmailValid(mail.getText().toString())) {
                    textInputLayoutEmail.setErrorEnabled(true);
                    textInputLayoutEmail.setError(getString(R.string.settings_suggest_not_valid_email));
                    return;
                } else {
                    textInputLayoutEmail.setErrorEnabled(false);
                }

                if (message.getText().toString().trim().equals("")) {
                    textInputLayoutMessage.setErrorEnabled(true);
                    textInputLayoutMessage.setError(getString(R.string.settings_suggest_field_mandatory));
                    return;
                } else {
                    textInputLayoutMessage.setErrorEnabled(false);
                }

                // Create and send the mail
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/email");

                intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"nanchenbastian@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "[Taskit] Suggest a Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, "Name: "+ name.getText()
                        +"\n" + "Email address: "+ mail.getText()
                        +"\n\n"+ message.getText());

                startActivity(Intent.createChooser(intent, "Suggest a feature:"));
            }
        });
    }

    /**
     * Method is used for checking valid email id format.
     *
     * @param email Text typed by the user
     * @return boolean True for valid false for invalid
     */
    private boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
