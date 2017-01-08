package ch.epfl.sweng.project.chat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

import ch.epfl.sweng.project.EditTaskActivity;
import ch.epfl.sweng.project.R;
import ch.epfl.sweng.project.Task;
import ch.epfl.sweng.project.User;
import ch.epfl.sweng.project.Utils;
import ch.epfl.sweng.project.data.FirebaseChatHelper;

/**
 * Class assuring that user can chat between each others when
 * clicking on the chat button in the details of the task.
 */
public class ChatActivity extends AppCompatActivity {
    public static final String TASK_CHAT_KEY = "ch.epfl.sweng.project.chat.TASK_CHAT_KEY";


    private Intent intent;
    private Task task;
    private FirebaseChatHelper chatHelper;
    private String currentUserName;
    private FloatingActionButton sendMssgButton;
    private String mail;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Initialise the Task and check its validity
        getAndCheckIntent();

        initializeToolbar();

        try {
            currentUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        }catch (NullPointerException e) {
            currentUserName = User.DEFAULT_NAME;
        }

        //Initialise the MessageAdapter
        MessageAdapter mAdapter = new MessageAdapter(this,
                R.layout.list_item_chat,
                task.getListOfMessages(),
                currentUserName);

        //Get the listView
        ListView mssgListView = (ListView) findViewById(R.id.list_of_messages);
        //Bind the adapter to the listView
        mssgListView.setAdapter(mAdapter);

        sendMssgButton = (FloatingActionButton) findViewById(R.id.send_message_button);
        sendMssgButton.setOnClickListener(new SendMessageOnClickListener());

        //Default behavior of the send message button
        sendMssgButton.setEnabled(false);

        EditText editMssg = (EditText) findViewById(R.id.input);
        editMssg.addTextChangedListener(new SendButtonWatcher());

        //Instantiation of the ChatHelper
        chatHelper = new FirebaseChatHelper(this, mAdapter);

        //Retrieve user email
        try {
            mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } catch (NullPointerException e) {
            mail = User.DEFAULT_EMAIL;
        }
        //Initiate the listener
        if(task.getHasNewMessages()) {
            chatHelper.setNewMessagesHasRead(mail, task);
        }
        chatHelper.retrieveMessages(mail, task);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        chatHelper.retrieveMessages(mail, task);
    }

    @Override
    protected void onStop() {
        super.onStop();
        chatHelper.removeListener();
    }

    private void getAndCheckIntent() {
        intent = getIntent();
        if (intent == null) {
            throw new IllegalArgumentException("Intent passed to ChatActivity is null");
        }
        getAndCheckIntentExtra();
    }

    private void getAndCheckIntentExtra() {
        task = intent.getParcelableExtra(TASK_CHAT_KEY);
        if (task == null) {
            throw new IllegalArgumentException("Task passed with the intent to ChatActivity is null");
        }
    }

    /**
     * Start the toolbar and enable that back button on the toolbar.
     *
     */
    private void initializeToolbar() {
        //Set toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        mToolbar.setTitle(Utils.separateTitleAndSuffix(task.getName())[0]);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new ReturnArrowListener());
    }

    private class SendMessageOnClickListener implements View.OnClickListener {

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            EditText editMssg = (EditText) findViewById(R.id.input);
            if (editMssg.getText() != null) {
                String mssgText = editMssg.getText().toString();

                if (!mssgText.isEmpty()) {
                    long time = new Date().getTime();
                    Message newMessage = new Message(currentUserName, mssgText, time);
                    chatHelper.updateChat(task, newMessage, mail);
                    editMssg.getText().clear();
                }
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
            Intent intent = new Intent(getApplicationContext(), EditTaskActivity.class);
            intent.putExtra(TASK_CHAT_KEY, task);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private class SendButtonWatcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            sendMssgButton.setEnabled(!s.toString().isEmpty());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    }
}
