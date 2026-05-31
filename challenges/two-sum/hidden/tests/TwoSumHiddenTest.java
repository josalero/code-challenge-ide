package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TwoSumHiddenTest {

    @Test
    void findsPairWithNegativeNumbers() {
        assertArrayEquals(new int[] {0, 2}, Solution.twoSum(new int[] {-1, -2, -3, -4, -5}, -8));
    }

    @Test
    void findsPairInLongerArray() {
        assertArrayEquals(new int[] {2, 4}, Solution.twoSum(new int[] {1, 5, 3, 7, 9}, 10));
    }
}
