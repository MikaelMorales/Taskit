package ch.epfl.sweng.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import com.facebook.FacebookSdk;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.project.data.FirebaseTaskHelper;
import ch.epfl.sweng.project.data.LocalTaskHelper;
import ch.epfl.sweng.project.data.TaskHelper;
import ch.epfl.sweng.project.data.TaskProvider;
import ch.epfl.sweng.project.data.UserProvider;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;


public class DatabaseTest extends SuperTest{

    @BeforeClass
    public static void setUserProvider() {
        UserProvider.setProvider(Utils.TEST_PROVIDER);
    }

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(
            MainActivity.class){
        //Override to be able to change the SharedPreferences effectively
        @Override
        protected void beforeActivityLaunched(){
            Context actualContext = InstrumentationRegistry.getTargetContext();
            SharedPreferences prefs = actualContext.getSharedPreferences(actualContext.getString(R.string.application_prefs_name), Context.MODE_PRIVATE);
            prefs.edit().putBoolean(actualContext.getString(R.string.first_launch), false).apply();
            prefs.edit().putBoolean(actualContext.getString(R.string.new_user), false).apply();
            FacebookSdk.sdkInitialize(actualContext);
            super.beforeActivityLaunched();
        }
    };

    private TaskHelper mDatabase;

    @Before
    public void initLocalDB()
    {
        TaskListAdapter mTaskAdapter;
        ArrayList<Task> taskList = new ArrayList<>();
        mTaskAdapter = new TaskListAdapter(mActivityRule.getActivity(), taskList);
        TaskProvider provider = new TaskProvider(mActivityRule.getActivity(), mTaskAdapter, taskList);
        mDatabase = provider.getTaskProvider();
    }


    @Test
    public void inTestInitLocalDB(){

        initLocalDB();
        assertThat(mDatabase, instanceOf(LocalTaskHelper.class));
    }

    @Test
    public void initFirebaseDb(){

        TaskListAdapter mTaskAdapter;
        ArrayList<Task> taskList = new ArrayList<>();

        mTaskAdapter = new TaskListAdapter(mActivityRule.getActivity(), taskList);
        mDatabase = new FirebaseTaskHelper(mActivityRule.getActivity(), mTaskAdapter, taskList);
        assertThat(mDatabase, instanceOf(FirebaseTaskHelper.class));
    }

    @Test
    public void retrieveAllDataFromFirebase() {

        // User
        String email = "trixyfinger@gmail.com";
        Location location1 = new Location("Office", 80, 89);
        Location location2 = new Location("Home", 80, 89);
        List<Location> listLocations = new ArrayList<>();
        listLocations.add(location2);
        listLocations.add(location1);
        User testUser1 = new User(email);

        initFirebaseDb();
        mDatabase.retrieveAllData(testUser1, false);
        assertEquals(testUser1.getEmail(), "trixyfinger@gmail.com");
    }
}

