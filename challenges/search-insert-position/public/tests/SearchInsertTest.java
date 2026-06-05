package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SearchInsertTest {

    @Test
    void expect_searchinsert_new_int_1_3_5_6_5_to_equal_2() {
        assertEquals(2, Solution.searchInsert(new int[] {1,3,5,6}, 5));
    }

    @Test
    void expect_searchinsert_new_int_1_3_5_6_2_to_equal_1() {
        assertEquals(1, Solution.searchInsert(new int[] {1,3,5,6}, 2));
    }
}
