package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PowerOfTwoHiddenTest {

    @Test
    void expect_ispoweroftwo_16_to_equal_true() {
        assertEquals(true, Solution.isPowerOfTwo(16));
    }

    @Test
    void expect_ispoweroftwo_0_to_equal_false() {
        assertEquals(false, Solution.isPowerOfTwo(0));
    }
}
