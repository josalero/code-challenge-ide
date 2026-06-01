package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class CountPrimesHiddenTest {

    @Test
    void expect_countprimes_100_to_equal_25() {
        assertEquals(25, Solution.countPrimes(100));
    }

    @Test
    void expect_countprimes_2_to_equal_0() {
        assertEquals(0, Solution.countPrimes(2));
    }
}
