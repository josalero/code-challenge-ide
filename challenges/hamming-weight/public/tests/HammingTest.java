package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class HammingTest {

    @Test
    void expect_hammingweight_11_to_equal_3() {
        assertEquals(3, Solution.hammingWeight(11));
    }

    @Test
    void expect_hammingweight_128_to_equal_1() {
        assertEquals(1, Solution.hammingWeight(128));
    }
}
