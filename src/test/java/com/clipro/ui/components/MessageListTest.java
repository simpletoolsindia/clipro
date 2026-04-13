package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageListTest {

    @Test
    void shouldCreateEmptyList() {
        MessageList list = new MessageList();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void shouldAddMessages() {
        MessageList list = new MessageList();
        list.addUser("hello");
        list.addAssistant("hi there");

        assertEquals(2, list.size());
        assertNotNull(list.get(0));
        assertNotNull(list.get(1));
    }

    @Test
    void shouldGetMessageByIndex() {
        MessageList list = new MessageList();
        list.addUser("first");
        list.addAssistant("second");

        Message msg = list.get(0);
        assertEquals("first", msg.getContent());
        assertEquals(MessageRole.USER, msg.getRole());
    }

    @Test
    void shouldReturnNullForInvalidIndex() {
        MessageList list = new MessageList();
        assertNull(list.get(-1));
        assertNull(list.get(0));
        assertNull(list.get(100));
    }

    @Test
    void shouldClear() {
        MessageList list = new MessageList();
        list.addUser("test");
        list.clear();

        assertTrue(list.isEmpty());
    }

    @Test
    void shouldScrollUp() {
        MessageList list = new MessageList();
        for (int i = 0; i < 10; i++) {
            list.addUser("msg " + i);
        }

        list.scrollUp(3);
        assertEquals(3, list.getScrollOffset());
    }

    @Test
    void shouldScrollDown() {
        MessageList list = new MessageList();
        for (int i = 0; i < 10; i++) {
            list.addUser("msg " + i);
        }

        list.scrollUp(5);
        list.scrollDown(2);
        assertEquals(3, list.getScrollOffset());
    }

    @Test
    void shouldRenderMessages() {
        MessageList list = new MessageList();
        list.addUser("hello");
        list.addAssistant("world");

        String output = list.render();
        assertTrue(output.contains("[USER]"));
        assertTrue(output.contains("hello"));
        assertTrue(output.contains("[1]"));
        assertTrue(output.contains("[2]"));
    }

    @Test
    void shouldRenderAllMessages() {
        MessageList list = new MessageList();
        list.addUser("one");
        list.addAssistant("two");

        String output = list.renderAll();
        assertTrue(output.contains("one"));
        assertTrue(output.contains("two"));
        assertTrue(output.contains("[1]"));
        assertTrue(output.contains("[2]"));
    }
}
