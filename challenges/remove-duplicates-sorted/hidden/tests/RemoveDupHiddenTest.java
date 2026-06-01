package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class RemoveDupHiddenTest {

    @Test
    void expect_removeduplicates_a_to_equal_5() {
        int[] a = {0,0,1,1,1,2,2,3,3,4};
        assertEquals(5, Solution.removeDuplicates(a));
    }
}
