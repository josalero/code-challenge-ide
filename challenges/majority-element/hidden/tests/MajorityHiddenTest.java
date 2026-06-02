package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MajorityHiddenTest {

    @Test
    void expect_majorityelement_new_int_1_to_equal_1() {
        assertEquals(1, Solution.majorityElement(new int[] {1}));
    }

    @Test
    void expect_majorityelement_new_int_5_5_5_2_2_to_equa() {
        assertEquals(5, Solution.majorityElement(new int[] {5,5,5,2,2}));
    }
}
