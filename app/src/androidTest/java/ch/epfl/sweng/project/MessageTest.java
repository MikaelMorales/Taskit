package ch.epfl.sweng.project;

import org.junit.Before;
import org.junit.Test;

import ch.epfl.sweng.project.chat.Message;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for Message
 */
public class MessageTest {

    private Message message;
    private String userName;
    private String body;
    private long time;

    @Before
    public void constructMessage() {
        userName = "user name test";
        body = "Message body test";
        time = 1234;
        message = new Message(userName, body, time);
    }

    @Test
    public void testUserNameGetter() {
        assertEquals(userName, message.getUserName());
    }

    @Test
    public void testBodyGetter() {
        assertEquals(body, message.getBody());
    }

    @Test
    public void testTimeGetter() {
        assertEquals(time, message.getTime());
    }

}
