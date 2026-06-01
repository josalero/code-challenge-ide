package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ContainsDuplicateHiddenTest {

    @Test
    void expect_containsduplicate_new_int_1_1_to_equal_tr() {
        assertEquals(true, Solution.containsDuplicate(new int[] {1,1}));
    }

    @Test
    void expect_containsduplicate_new_int_to_equal_false() {
        assertEquals(false, Solution.containsDuplicate(new int[] {}));
    }
}
