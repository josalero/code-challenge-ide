package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MergeTest {

    @Test
    void verify_behavior_for_merge_sorted_arrays() {
        int[] a = {1,2,3,0,0,0};
        Solution.merge(a, 3, new int[] {2,5,6}, 3);
        assertArrayEquals(new int[] {1,2,2,3,5,6}, a);
    }
}
