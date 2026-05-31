package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FizzBuzzHiddenTest {

    @Test
    void fizzBuzzForZeroIsEmpty() {
        assertEquals(List.of(), Solution.fizzBuzz(0));
    }

    @Test
    void fizzBuzzForThirtyHasExpectedLength() {
        List<String> result = Solution.fizzBuzz(30);
        assertEquals(30, result.size());
        assertEquals("FizzBuzz", result.get(29));
    }
}
