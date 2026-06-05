package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MoveZeroesTest {

    @Test
    void verify_behavior_for_move_zeroes() {
        int[] a = {0,1,0,3,12};
        Solution.moveZeroes(a);
        assertArrayEquals(new int[] {1,3,12,0}, a);
    }
}
