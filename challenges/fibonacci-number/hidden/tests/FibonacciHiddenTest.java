package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class FibonacciHiddenTest {

    @Test
    void expect_fib_10_to_equal_55l() {
        assertEquals(55L, Solution.fib(10));
    }

    @Test
    void expect_fib_6_to_equal_8l() {
        assertEquals(8L, Solution.fib(6));
    }
}
