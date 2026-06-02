package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SingleNumberHiddenTest {

    @Test
    void expect_singlenumber_new_int_3_to_equal_3() {
        assertEquals(3, Solution.singleNumber(new int[] {3}));
    }

    @Test
    void expect_singlenumber_new_int_7_5_7_to_equal_5() {
        assertEquals(5, Solution.singleNumber(new int[] {7,5,7}));
    }
}
