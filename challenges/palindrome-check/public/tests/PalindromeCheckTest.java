package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PalindromeCheckTest {

    @Test
    void emptyStringIsPalindrome() {
        assertTrue(Solution.isPalindrome(""));
    }

    @Test
    void singleCharacterIsPalindrome() {
        assertTrue(Solution.isPalindrome("a"));
    }

    @Test
    void racecarIsPalindrome() {
        assertTrue(Solution.isPalindrome("racecar"));
    }

    @Test
    void helloIsNotPalindrome() {
        assertFalse(Solution.isPalindrome("hello"));
    }
}
