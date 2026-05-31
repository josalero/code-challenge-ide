package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidParenthesesTest {

    @Test
    void simplePairIsValid() {
        assertTrue(Solution.isValid("()"));
    }

    @Test
    void mixedBracketsAreValid() {
        assertTrue(Solution.isValid("()[]{}"));
    }

    @Test
    void mismatchedClosingIsInvalid() {
        assertFalse(Solution.isValid("(]"));
    }

    @Test
    void wrongOrderIsInvalid() {
        assertFalse(Solution.isValid("([)]"));
    }
}
