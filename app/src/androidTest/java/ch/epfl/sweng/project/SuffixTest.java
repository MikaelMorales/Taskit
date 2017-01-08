package ch.epfl.sweng.project;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SuffixTest {

    private String title;
    private String creator;
    private String sharer;
    private String taskTitle;
    private String suffix;
    private String otherSharer;
    private String alternateTaskTitle;

    @Before
    public void setup(){
        title = "test";
        creator = "Alice";
        sharer = "Bob";
        taskTitle = "test@@Alice@@Bob";
        suffix = "@@Alice@@Bob";
        otherSharer ="Candea";
        alternateTaskTitle = "test@@Alice@@Candea";
    }

    @Test
    public void constructSharedTitleTest(){
        String result = Utils.constructSharedTitle(title, creator, sharer);
        assertTrue(result.equals(taskTitle));
    }

    @Test
    public void separateTitleAndSuffixTest(){
        String[] result = Utils.separateTitleAndSuffix(taskTitle);
        assertTrue(result[0].equals(title));
        assertTrue(result[1].equals(suffix));

        result = Utils.separateTitleAndSuffix(title);
        assertTrue(result[0].equals(title));
        assertTrue(result[1].equals(""));

    }

    @Test
    public void getCreatorAndSharerTest(){
        String[] result = Utils.getCreatorAndSharer(suffix);
        assertTrue(result[0].equals(creator));
        assertTrue(result[1].equals(sharer));
    }

    @Test
    public void identityCarriedByAllUtilsOperation(){
        String resultTitle = Utils.constructSharedTitle(title, creator, sharer);
        String[] result = Utils.separateTitleAndSuffix(resultTitle);
        String[] suffixResult = Utils.getCreatorAndSharer(result[1]);
        assertTrue(result[0].equals(title));
        assertTrue(suffixResult[0].equals(creator));
        assertTrue(suffixResult[1].equals(sharer));
    }

    @Test
    public void sharedTaskPreProcessingTest(){
        String descriptionTest = "new description test";

        String locationNameTest = "locationName test workplace";
        Date dueDateTest = new Date(0);
        long durationTest = 55;
        Task.Energy energyTest = Task.Energy.LOW;
        String authorTest = "A test author";
        List<String> listContributorsTest = new ArrayList<>();
        listContributorsTest.add(authorTest);

        Task newTaskTest = new Task(taskTitle, descriptionTest, locationNameTest, dueDateTest, durationTest, energyTest.toString(), listContributorsTest, 0L, false);
        assertTrue(Utils.sharedTaskPreProcessing(newTaskTest, otherSharer).getName().equals(alternateTaskTitle));
    }
}
