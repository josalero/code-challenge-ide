package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class RomanHiddenTest {

    @Test
    void expect_romantoint_mcmxciv_to_equal_1994() {
        assertEquals(1994, Solution.romanToInt("MCMXCIV"));
    }

    @Test
    void expect_romantoint_ix_to_equal_9() {
        assertEquals(9, Solution.romanToInt("IX"));
    }
}
