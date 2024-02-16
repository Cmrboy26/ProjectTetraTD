package net.cmr.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GameManagerTest {

    @Test
    public void test() {
        System.out.println("TESTS");
    }

    @Test
    public void failTest() {
        assertEquals(1, 2);
    }

    @Test
    public void testSimple() throws Exception {
        assertTrue(false);
    }

}

