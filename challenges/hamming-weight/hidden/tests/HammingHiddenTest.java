package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class HammingHiddenTest {

    @Test
    void expect_hammingweight_1_to_equal_31() {
        assertEquals(31, Solution.hammingWeight(-1));
    }

    @Test
    void expect_hammingweight_0_to_equal_0() {
        assertEquals(0, Solution.hammingWeight(0));
    }
}
