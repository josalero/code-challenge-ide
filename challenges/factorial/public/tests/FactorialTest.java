package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class FactorialTest {

    @Test
    void expect_factorial_0_to_equal_1l() {
        assertEquals(1L, Solution.factorial(0));
    }

    @Test
    void expect_factorial_5_to_equal_120l() {
        assertEquals(120L, Solution.factorial(5));
    }

    @Test
    void expect_factorial_1_to_equal_1l() {
        assertEquals(1L, Solution.factorial(1));
    }
}
