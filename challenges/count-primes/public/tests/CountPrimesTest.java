package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class CountPrimesTest {

    @Test
    void expect_countprimes_10_to_equal_4() {
        assertEquals(4, Solution.countPrimes(10));
    }

    @Test
    void expect_countprimes_0_to_equal_0() {
        assertEquals(0, Solution.countPrimes(0));
    }
}
