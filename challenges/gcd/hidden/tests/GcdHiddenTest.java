package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class GcdHiddenTest {

    @Test
    void expect_gcd_48_18_to_equal_12() {
        assertEquals(12, Solution.gcd(48, 18));
    }

    @Test
    void expect_gcd_0_7_to_equal_7() {
        assertEquals(7, Solution.gcd(0, 7));
    }
}
