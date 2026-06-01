package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class RotateHiddenTest {

    @Test
    void verify_behavior_for_rotate_array() {
        int[] a = {-1,-100,3,99};
        Solution.rotate(a, 2);
        assertArrayEquals(new int[] {3,99,-1,-100}, a);
    }
}
