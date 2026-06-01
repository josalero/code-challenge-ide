package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class LinearSearchHiddenTest {

    @Test
    void expect_linearsearch_new_int_9_9_to_equal_0() {
        assertEquals(0, Solution.linearSearch(new int[] {9}, 9));
    }

    @Test
    void expect_linearsearch_new_int_1_to_equal_1() {
        assertEquals(-1, Solution.linearSearch(new int[] {}, 1));
    }
}
