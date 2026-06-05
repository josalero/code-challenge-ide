package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class LinearSearchTest {

    @Test
    void expect_linearsearch_new_int_2_3_4_3_to_equal_1() {
        assertEquals(1, Solution.linearSearch(new int[] {2,3,4}, 3));
    }

    @Test
    void expect_linearsearch_new_int_1_2_5_to_equal_1() {
        assertEquals(-1, Solution.linearSearch(new int[] {1,2}, 5));
    }
}
