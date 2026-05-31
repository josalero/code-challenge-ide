package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidParenthesesHiddenTest {

    @Test
    void nestedBracketsAreValid() {
        assertTrue(Solution.isValid("{[]}"));
    }

    @Test
    void extraOpeningBracketIsInvalid() {
        assertFalse(Solution.isValid("(("));
    }
}
