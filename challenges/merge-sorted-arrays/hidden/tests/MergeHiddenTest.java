package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MergeHiddenTest {

    @Test
    void verify_behavior_for_merge_sorted_arrays() {
        int[] a = {1,0,0,0};
        Solution.merge(a, 1, new int[] {2,3}, 2);
        assertArrayEquals(new int[] {1,2,3}, a);
    }
}
