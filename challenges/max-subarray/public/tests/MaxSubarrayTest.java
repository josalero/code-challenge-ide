package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MaxSubarrayTest {

    @Test
    void expect_maxsubarray_new_int_2_1_3_4_1_2_1_5_4_to_() {
        assertEquals(6, Solution.maxSubArray(new int[] {-2,1,-3,4,-1,2,1,-5,4}));
    }

    @Test
    void expect_maxsubarray_new_int_1_to_equal_1() {
        assertEquals(1, Solution.maxSubArray(new int[] {1}));
    }
}
