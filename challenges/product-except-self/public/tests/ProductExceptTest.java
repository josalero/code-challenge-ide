package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ProductExceptTest {

    @Test
    void expect_productexceptself_new_int_1_2_3_4_to_retu() {
        assertArrayEquals(new int[] {24,12,8,6}, Solution.productExceptSelf(new int[] {1,2,3,4}));
    }

    @Test
    void expect_productexceptself_new_int_1_1_0_3_3_to_re() {
        assertArrayEquals(new int[] {0,0,9,0}, Solution.productExceptSelf(new int[] {-1,1,0,-3,3}));
    }
}
