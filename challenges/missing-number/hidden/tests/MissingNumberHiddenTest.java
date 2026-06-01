package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MissingNumberHiddenTest {

    @Test
    void expect_missingnumber_new_int_9_6_4_2_3_5_7_0_1_t() {
        assertEquals(8, Solution.missingNumber(new int[] {9,6,4,2,3,5,7,0,1}));
    }

    @Test
    void expect_missingnumber_new_int_1_to_equal_0() {
        assertEquals(0, Solution.missingNumber(new int[] {1}));
    }
}
