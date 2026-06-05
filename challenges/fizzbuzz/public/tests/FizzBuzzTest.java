package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FizzBuzzTest {

    @Test
    void fizzBuzzForOne() {
        assertEquals(List.of("1"), Solution.fizzBuzz(1));
    }

    @Test
    void fizzBuzzForFive() {
        assertEquals(List.of("1", "2", "Fizz", "4", "Buzz"), Solution.fizzBuzz(5));
    }

    @Test
    void fizzBuzzIncludesFizzBuzzAtFifteen() {
        List<String> result = Solution.fizzBuzz(15);
        assertEquals("FizzBuzz", result.get(14));
    }
}
