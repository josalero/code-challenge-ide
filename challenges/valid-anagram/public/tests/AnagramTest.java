package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class AnagramTest {

    @Test
    void expect_isanagram_anagram_nagaram_to_equal_true() {
        assertEquals(true, Solution.isAnagram("anagram", "nagaram"));
    }

    @Test
    void expect_isanagram_rat_car_to_equal_false() {
        assertEquals(false, Solution.isAnagram("rat", "car"));
    }
}
