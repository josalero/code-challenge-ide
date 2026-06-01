package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MaxSubarrayHiddenTest {

    @Test
    void expect_maxsubarray_new_int_5_4_1_7_8_to_equal_23() {
        assertEquals(23, Solution.maxSubArray(new int[] {5,4,-1,7,8}));
    }

    @Test
    void expect_maxsubarray_new_int_2_1_to_equal_2() {
        assertEquals(-2, Solution.maxSubArray(new int[] {-2,-1}));
    }
}
