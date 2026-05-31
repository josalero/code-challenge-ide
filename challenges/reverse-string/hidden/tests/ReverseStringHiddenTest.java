package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReverseStringHiddenTest {

    @Test
    void reversesPalindromeUnchanged() {
        assertEquals("racecar", Solution.reverse("racecar"));
    }

    @Test
    void reversesMixedCase() {
        assertEquals("avaJ", Solution.reverse("Java"));
    }
}
