package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MajorityTest {

    @Test
    void expect_majorityelement_new_int_3_2_3_to_equal_3() {
        assertEquals(3, Solution.majorityElement(new int[] {3,2,3}));
    }

    @Test
    void expect_majorityelement_new_int_2_2_1_1_1_2_2_to_() {
        assertEquals(2, Solution.majorityElement(new int[] {2,2,1,1,1,2,2}));
    }
}
