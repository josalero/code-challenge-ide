package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PlusOneHiddenTest {

    @Test
    void expect_plusone_new_int_9_9_9_to_return_1_0_0_0() {
        assertArrayEquals(new int[] {1,0,0,0}, Solution.plusOne(new int[] {9,9,9}));
    }

    @Test
    void expect_plusone_new_int_9_to_return_10() {
        assertArrayEquals(new int[] {10}, Solution.plusOne(new int[] {9}));
    }
}
