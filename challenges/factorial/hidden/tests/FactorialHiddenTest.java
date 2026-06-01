package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class FactorialHiddenTest {

    @Test
    void expect_factorial_10_to_equal_3628800l() {
        assertEquals(3628800L, Solution.factorial(10));
    }

    @Test
    void expect_factorial_3_to_equal_6l() {
        assertEquals(6L, Solution.factorial(3));
    }
}
