package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class RotateTest {

    @Test
    void verify_behavior_for_rotate_array() {
        int[] a = {1,2,3,4,5,6,7};
        Solution.rotate(a, 3);
        assertArrayEquals(new int[] {5,6,7,1,2,3,4}, a);
    }
}
