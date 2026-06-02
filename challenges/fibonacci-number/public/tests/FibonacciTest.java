package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class FibonacciTest {

    @Test
    void expect_fib_0_to_equal_0l() {
        assertEquals(0L, Solution.fib(0));
    }

    @Test
    void expect_fib_1_to_equal_1l() {
        assertEquals(1L, Solution.fib(1));
    }

    @Test
    void expect_fib_5_to_equal_5l() {
        assertEquals(5L, Solution.fib(5));
    }
}
