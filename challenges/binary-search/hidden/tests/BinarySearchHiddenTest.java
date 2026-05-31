package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinarySearchHiddenTest {

    @Test
    void findsLastElement() {
        assertEquals(4, Solution.binarySearch(new int[] {1, 3, 5, 7, 9}, 9));
    }

    @Test
    void emptyArrayReturnsMinusOne() {
        assertEquals(-1, Solution.binarySearch(new int[] {}, 1));
    }
}
