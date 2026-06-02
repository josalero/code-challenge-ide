package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PrimeHiddenTest {

    @Test
    void expect_isprime_15_to_equal_false() {
        assertEquals(false, Solution.isPrime(15));
    }

    @Test
    void expect_isprime_97_to_equal_true() {
        assertEquals(true, Solution.isPrime(97));
    }
}
