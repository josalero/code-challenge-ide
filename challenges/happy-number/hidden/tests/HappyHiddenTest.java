package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class HappyHiddenTest {

    @Test
    void expect_ishappy_1_to_equal_true() {
        assertEquals(true, Solution.isHappy(1));
    }

    @Test
    void expect_ishappy_4_to_equal_false() {
        assertEquals(false, Solution.isHappy(4));
    }
}
