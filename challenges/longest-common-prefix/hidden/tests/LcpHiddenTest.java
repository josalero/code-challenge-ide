package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class LcpHiddenTest {

    @Test
    void expect_longestcommonprefix_new_string_to_equal() {
        assertEquals("", Solution.longestCommonPrefix(new String[] {}));
    }

    @Test
    void expect_longestcommonprefix_new_string_a_to_equal() {
        assertEquals("a", Solution.longestCommonPrefix(new String[] {"a"}));
    }
}
