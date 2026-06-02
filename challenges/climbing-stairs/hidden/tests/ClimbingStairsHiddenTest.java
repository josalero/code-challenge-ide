package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ClimbingStairsHiddenTest {

    @Test
    void expect_climbstairs_10_to_equal_89() {
        assertEquals(89, Solution.climbStairs(10));
    }

    @Test
    void expect_climbstairs_1_to_equal_1() {
        assertEquals(1, Solution.climbStairs(1));
    }
}
