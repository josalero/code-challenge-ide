package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PowerOfTwoTest {

    @Test
    void expect_ispoweroftwo_1_to_equal_true() {
        assertEquals(true, Solution.isPowerOfTwo(1));
    }

    @Test
    void expect_ispoweroftwo_3_to_equal_false() {
        assertEquals(false, Solution.isPowerOfTwo(3));
    }
}
