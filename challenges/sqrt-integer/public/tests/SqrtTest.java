package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SqrtTest {

    @Test
    void expect_mysqrt_8_to_equal_2() {
        assertEquals(2, Solution.mySqrt(8));
    }

    @Test
    void expect_mysqrt_0_to_equal_0() {
        assertEquals(0, Solution.mySqrt(0));
    }
}
