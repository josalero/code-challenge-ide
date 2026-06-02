"""Tests for example row generation."""

from example_rows import parse_row_from_description, rows_from_meta


def test_two_sum_meta_parses() -> None:
    meta = [
        {
            "description": "Expect twoSum(new int[] {2, 7, 11, 15}, 9) to return [0, 1]",
        },
    ]
    rows = rows_from_meta(meta, "two-sum")
    assert rows == [("[2, 7, 11, 15], 9", "[0,1]")]


def test_binary_search_parses() -> None:
    row = parse_row_from_description(
        "Expect binarySearch(new int[] {1, 3, 5, 7, 9}, 3) to equal 1"
    )
    assert row is not None
    assert row[0] == "[1, 3, 5, 7, 9], 3"
    assert row[1] == "1"


def test_vague_description_skipped() -> None:
    assert parse_row_from_description("Verify behavior for merge-sorted-arrays") is None


def test_anagram_groups_json() -> None:
    row = parse_row_from_description(
        'Expect groupAnagrams(new String[] {"eat", "tea"}) to equal [["eat","tea"]]'
    )
    assert row is not None
    assert row[0] == '["eat", "tea"]'
