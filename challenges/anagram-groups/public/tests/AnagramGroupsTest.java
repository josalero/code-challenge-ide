package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnagramGroupsTest {

    @Test
    void groupsClassicExample() {
        List<List<String>> expected = List.of(
                List.of("bat"),
                List.of("nat", "tan"),
                List.of("eat", "tea", "ate"));
        assertGroupedEquals(expected, Solution.groupAnagrams(new String[] {"eat", "tea", "tan", "ate", "nat", "bat"}));
    }

    @Test
    void emptyInputReturnsEmptyGroups() {
        assertEquals(List.of(), Solution.groupAnagrams(new String[] {}));
    }

    @Test
    void singleWordFormsOneGroup() {
        assertGroupedEquals(List.of(List.of("a")), Solution.groupAnagrams(new String[] {"a"}));
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
