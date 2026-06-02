package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MoveZeroesHiddenTest {

    @Test
    void verify_behavior_for_move_zeroes() {
        int[] a = {0,0,1};
        Solution.moveZeroes(a);
        assertArrayEquals(new int[] {1,0,0}, a);
    }
}
