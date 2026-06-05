package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MissingNumberTest {

    @Test
    void expect_missingnumber_new_int_3_0_1_to_equal_2() {
        assertEquals(2, Solution.missingNumber(new int[] {3,0,1}));
    }

    @Test
    void expect_missingnumber_new_int_0_to_equal_1() {
        assertEquals(1, Solution.missingNumber(new int[] {0}));
    }
}
