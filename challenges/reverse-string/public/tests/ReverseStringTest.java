package com.challenge.public_;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReverseStringTest {

    @Test
    void reversesSimpleWord() {
        assertEquals("olleh", Solution.reverse("hello"));
    }

    @Test
    void reversesEmptyString() {
        assertEquals("", Solution.reverse(""));
    }

    @Test
    void reversesSingleCharacter() {
        assertEquals("a", Solution.reverse("a"));
    }
}
