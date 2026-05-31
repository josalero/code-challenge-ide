package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnagramGroupsHiddenTest {

    @Test
    void groupsAllDistinctWordsSeparately() {
        List<List<String>> expected = List.of(
                List.of("abc"),
                List.of("def"),
                List.of("ghi"));
        assertGroupedEquals(expected, Solution.groupAnagrams(new String[] {"abc", "def", "ghi"}));
    }

    @Test
    void groupsDuplicateAnagramForms() {
        List<List<String>> expected = List.of(List.of("aab", "aba", "baa"));
        assertGroupedEquals(expected, Solution.groupAnagrams(new String[] {"aab", "aba", "baa"}));
    }

    private static void assertGroupedEquals(List<List<String>> expected, List<List<String>> actual) {
        assertEquals(normalize(expected), normalize(actual));
    }

    private static List<List<String>> normalize(List<List<String>> groups) {
        List<List<String>> normalized = new ArrayList<>();
        for (List<String> group : groups) {
            List<String> sortedGroup = new ArrayList<>(group);
            sortedGroup.sort(Comparator.naturalOrder());
            normalized.add(sortedGroup);
        }
        normalized.sort(Comparator.comparing(group -> group.get(0)));
        return normalized;
    }
}
