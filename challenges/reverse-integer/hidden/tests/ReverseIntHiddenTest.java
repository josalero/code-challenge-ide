package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ReverseIntHiddenTest {

    @Test
    void expect_reverse_1534236469_to_equal_0() {
        assertEquals(0, Solution.reverse(1534236469));
    }

    @Test
    void expect_reverse_120_to_equal_21() {
        assertEquals(21, Solution.reverse(120));
    }
}
