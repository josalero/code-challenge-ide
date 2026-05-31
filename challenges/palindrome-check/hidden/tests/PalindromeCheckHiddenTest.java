package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PalindromeCheckHiddenTest {

    @Test
    void abbaIsPalindrome() {
        assertTrue(Solution.isPalindrome("abba"));
    }

    @Test
    void abcaIsNotPalindrome() {
        assertFalse(Solution.isPalindrome("abca"));
    }
}
