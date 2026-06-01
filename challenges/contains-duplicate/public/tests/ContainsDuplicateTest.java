package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ContainsDuplicateTest {

    @Test
    void expect_containsduplicate_new_int_1_2_3_1_to_equa() {
        assertEquals(true, Solution.containsDuplicate(new int[] {1,2,3,1}));
    }

    @Test
    void expect_containsduplicate_new_int_1_2_3_4_to_equa() {
        assertEquals(false, Solution.containsDuplicate(new int[] {1,2,3,4}));
    }
}
