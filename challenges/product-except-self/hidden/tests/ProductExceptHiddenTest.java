package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ProductExceptHiddenTest {

    @Test
    void expect_productexceptself_new_int_5_to_return_1() {
        assertArrayEquals(new int[] {1}, Solution.productExceptSelf(new int[] {5}));
    }

    @Test
    void expect_productexceptself_new_int_2_2_to_return_2() {
        assertArrayEquals(new int[] {2,2}, Solution.productExceptSelf(new int[] {2,2}));
    }
}
