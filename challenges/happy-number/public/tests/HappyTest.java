package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class HappyTest {

    @Test
    void expect_ishappy_19_to_equal_true() {
        assertEquals(true, Solution.isHappy(19));
    }

    @Test
    void expect_ishappy_2_to_equal_false() {
        assertEquals(false, Solution.isHappy(2));
    }
}
