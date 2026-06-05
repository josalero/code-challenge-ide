package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class LcpTest {

    @Test
    void expect_longestcommonprefix_new_string_flower_flo() {
        assertEquals("fl", Solution.longestCommonPrefix(new String[] {"flower","flow","flight"}));
    }

    @Test
    void expect_longestcommonprefix_new_string_dog_raceca() {
        assertEquals("", Solution.longestCommonPrefix(new String[] {"dog","racecar","car"}));
    }
}
