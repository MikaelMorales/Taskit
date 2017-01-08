package ch.epfl.sweng.project;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


/**
 * Unit tests!
 */
@RunWith(AndroidJUnit4.class)
public class TaskTest {
    @Rule
    public final ExpectedException thrownException = ExpectedException.none();

    private Task testTask;

    @Before
    public void initValidValues() {
        String taskName = "test task";
        String taskDescription = "Task built with all parameters";
        String location = "Office";
        Date dueDate = new Date(3);
        String author = "Arthur Rimbaud";
        Task.Energy energy = Task.Energy.HIGH;
        long duration = 60;
        List<String> listOfContributors = new ArrayList<>();
        listOfContributors.add(author);

        testTask = new Task(taskName, taskDescription, location, dueDate, duration, energy.toString(), listOfContributors, 0, false);
    }

    /**
     * Test that the getters and setters work correctly with parameters
     */
    @Test
    public void testConstructorWithAllParameters() {
        String nameTest = "new name Test";
        String descriptionTest = "new description test";

        String locationNameTest = "locationName test workplace";
        Date dueDateTest = new Date(0);
        long durationTest = 55;
        Task.Energy energyTest = Task.Energy.LOW;
        String authorTest = "A test author";
        List<String> listContributorsTest = new ArrayList<>();
        listContributorsTest.add(authorTest);

        Task newTaskTest = new Task(nameTest, descriptionTest, locationNameTest, dueDateTest, durationTest, energyTest.toString(), listContributorsTest, 0L, false);


        assertEquals(nameTest, newTaskTest.getName());
        assertEquals(descriptionTest, newTaskTest.getDescription());
        assertEquals(locationNameTest, newTaskTest.getLocationName());
        assertEquals(dueDateTest.getTime(), newTaskTest.getDueDate().getTime());
        assertEquals(durationTest, newTaskTest.getDurationInMinutes());
        assertEquals(energyTest, newTaskTest.getEnergy());
        assertEquals(0L, newTaskTest.getIfNewContributor());
    }

    /**
     * Test that the setters modify correctly the Task
     */
    @Test
    public void testTaskSettersName() {
        String newName = "another name";
        testTask.setName(newName);
        assertEquals(newName, testTask.getName());
    }

    @Test
    public void testTaskSetDescription() {
        String newDescription = "This is a new description";
        testTask.setDescription(newDescription);
        assertEquals(newDescription, testTask.getDescription());
    }

    @Test
    public void testTaskSetLocationName() {
        String newLocationName = "Home";

        testTask.setLocationName(newLocationName);

        assertEquals(newLocationName, testTask.getLocationName());
    }

    @Test
    public void testTaskSetDueDate() {
        Date newDueDate = new Date(0);
        testTask.setDueDate(newDueDate);
        assertEquals(0, testTask.getDueDate().getTime());
    }

    @Test
    public void testTaskSetEnergy() {
        testTask.setEnergyNeeded(Task.Energy.NORMAL);
        assertEquals(Task.Energy.NORMAL, testTask.getEnergy());
    }

    @Test
    public void testTaskSetDuration() {
        testTask.setDurationInMinutes(23);
        assertEquals(23, testTask.getDurationInMinutes());
    }

    @Test
    public void testTaskSetAuthor() {
        testTask.addContributor("New author");
        assertTrue(testTask.getListOfContributors().contains("New author"));
    }

    /**
     * Test that the setName setter throws an IllegalArgumentException
     * when its argument is null
     */
    @Test
    public void testTaskSetNameException() {
        thrownException.expect(IllegalArgumentException.class);
        testTask.setName(null);
    }

    /**
     * Test that the setDescription setter throws an IllegalArgumentException
     * when its argument is null
     */
    @Test
    public void testTaskSetDescriptionException() {
        thrownException.expect(IllegalArgumentException.class);
        testTask.setDescription(null);
    }

    @Test
    public void testTaskSetDueDateException() {
        thrownException.expect(IllegalArgumentException.class);
        testTask.setDueDate(null);

    }

    @Test
    public void testTaskSetEnergyException() {
        thrownException.expect(IllegalArgumentException.class);
        testTask.setEnergyNeeded(null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testTaskConstructorException() {
        thrownException.expect(NullPointerException.class);

        new Task(null, null, null, null, 0, null, null, 0L, false);

    }

    /**
     * Test the describeContents method
     */
    @Test
    public void testDescribeContents() {
        assertEquals(0, testTask.describeContents());
    }
    /**
     * Test that an added Task has been correctly deleted when clicking on Delete.
     */

}
