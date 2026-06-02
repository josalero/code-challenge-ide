package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ClimbingStairsTest {

    @Test
    void expect_climbstairs_2_to_equal_2() {
        assertEquals(2, Solution.climbStairs(2));
    }

    @Test
    void expect_climbstairs_3_to_equal_3() {
        assertEquals(3, Solution.climbStairs(3));
    }
}
