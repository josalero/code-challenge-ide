package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MaxProfitTest {

    @Test
    void expect_maxprofit_new_int_7_1_5_3_6_4_to_equal_5() {
        assertEquals(5, Solution.maxProfit(new int[] {7,1,5,3,6,4}));
    }

    @Test
    void expect_maxprofit_new_int_7_6_4_3_1_to_equal_0() {
        assertEquals(0, Solution.maxProfit(new int[] {7,6,4,3,1}));
    }
}
