package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SingleNumberTest {

    @Test
    void expect_singlenumber_new_int_2_2_1_to_equal_1() {
        assertEquals(1, Solution.singleNumber(new int[] {2,2,1}));
    }

    @Test
    void expect_singlenumber_new_int_4_1_2_1_2_to_equal_4() {
        assertEquals(4, Solution.singleNumber(new int[] {4,1,2,1,2}));
    }
}
