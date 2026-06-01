package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MaxProfitHiddenTest {

    @Test
    void expect_maxprofit_new_int_1_2_3_4_5_to_equal_4() {
        assertEquals(4, Solution.maxProfit(new int[] {1,2,3,4,5}));
    }

    @Test
    void expect_maxprofit_new_int_2_2_2_to_equal_0() {
        assertEquals(0, Solution.maxProfit(new int[] {2,2,2}));
    }
}
