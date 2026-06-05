package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinarySearchTest {

    @Test
    void findsExistingTarget() {
        assertEquals(1, Solution.binarySearch(new int[] {1, 3, 5, 7, 9}, 3));
    }

    @Test
    void returnsMinusOneWhenMissing() {
        assertEquals(-1, Solution.binarySearch(new int[] {1, 3, 5, 7, 9}, 4));
    }

    @Test
    void findsFirstElement() {
        assertEquals(0, Solution.binarySearch(new int[] {2, 4, 6}, 2));
    }
}
