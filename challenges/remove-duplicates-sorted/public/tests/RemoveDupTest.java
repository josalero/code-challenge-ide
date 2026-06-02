package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class RemoveDupTest {

    @Test
    void expect_removeduplicates_a_to_equal_2() {
        int[] a = {1,1,2};
        assertEquals(2, Solution.removeDuplicates(a));
        assertEquals(1, a[0]);
        assertEquals(2, a[1]);
    }
}
