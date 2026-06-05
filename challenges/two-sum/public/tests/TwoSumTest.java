package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TwoSumTest {

    @Test
    void findsPairInSimpleCase() {
        assertArrayEquals(new int[] {0, 1}, Solution.twoSum(new int[] {2, 7, 11, 15}, 9));
    }

    @Test
    void findsPairWhenTargetUsesLaterIndices() {
        assertArrayEquals(new int[] {1, 2}, Solution.twoSum(new int[] {3, 2, 4}, 6));
    }

    @Test
    void findsPairWithDuplicateValues() {
        assertArrayEquals(new int[] {0, 1}, Solution.twoSum(new int[] {3, 3}, 6));
    }
}
