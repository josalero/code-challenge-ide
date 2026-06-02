package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PlusOneTest {

    @Test
    void expect_plusone_new_int_1_2_3_to_return_1_2_4() {
        assertArrayEquals(new int[] {1,2,4}, Solution.plusOne(new int[] {1,2,3}));
    }

    @Test
    void expect_plusone_new_int_0_to_return_1() {
        assertArrayEquals(new int[] {1}, Solution.plusOne(new int[] {0}));
    }
}
