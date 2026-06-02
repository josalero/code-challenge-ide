package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class RomanTest {

    @Test
    void expect_romantoint_iii_to_equal_3() {
        assertEquals(3, Solution.romanToInt("III"));
    }

    @Test
    void expect_romantoint_lviii_to_equal_58() {
        assertEquals(58, Solution.romanToInt("LVIII"));
    }
}
