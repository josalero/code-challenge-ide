package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SearchInsertHiddenTest {

    @Test
    void expect_searchinsert_new_int_1_3_5_6_7_to_equal_4() {
        assertEquals(4, Solution.searchInsert(new int[] {1,3,5,6}, 7));
    }

    @Test
    void expect_searchinsert_new_int_1_3_5_6_0_to_equal_0() {
        assertEquals(0, Solution.searchInsert(new int[] {1,3,5,6}, 0));
    }
}
