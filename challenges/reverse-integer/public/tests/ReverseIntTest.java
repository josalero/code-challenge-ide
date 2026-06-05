package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ReverseIntTest {

    @Test
    void expect_reverse_123_to_equal_321() {
        assertEquals(321, Solution.reverse(123));
    }

    @Test
    void expect_reverse_123_to_equal_321_2() {
        assertEquals(-321, Solution.reverse(-123));
    }
}
