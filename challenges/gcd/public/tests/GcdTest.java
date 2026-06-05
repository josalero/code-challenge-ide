package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class GcdTest {

    @Test
    void expect_gcd_54_24_to_equal_6() {
        assertEquals(6, Solution.gcd(54, 24));
    }

    @Test
    void expect_gcd_17_13_to_equal_1() {
        assertEquals(1, Solution.gcd(17, 13));
    }

    @Test
    void expect_gcd_25_15_to_equal_5() {
        assertEquals(5, Solution.gcd(25, 15));
    }
}
