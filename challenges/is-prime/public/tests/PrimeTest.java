package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PrimeTest {

    @Test
    void expect_isprime_1_to_equal_false() {
        assertEquals(false, Solution.isPrime(1));
    }

    @Test
    void expect_isprime_2_to_equal_true() {
        assertEquals(true, Solution.isPrime(2));
    }

    @Test
    void expect_isprime_17_to_equal_true() {
        assertEquals(true, Solution.isPrime(17));
    }
}
