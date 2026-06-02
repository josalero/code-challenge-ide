package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SqrtHiddenTest {

    @Test
    void expect_mysqrt_10_to_equal_3() {
        assertEquals(3, Solution.mySqrt(10));
    }

    @Test
    void expect_mysqrt_2147483647_to_equal_46340() {
        assertEquals(46340, Solution.mySqrt(2147483647));
    }
}
