package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class AnagramHiddenTest {

    @Test
    void expect_isanagram_listen_silent_to_equal_true() {
        assertEquals(true, Solution.isAnagram("listen", "silent"));
    }

    @Test
    void expect_isanagram_to_equal_true() {
        assertEquals(true, Solution.isAnagram("", ""));
    }
}
