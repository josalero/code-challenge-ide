"""Tests for C++ string literal escaping in generated Catch2 tests."""

from test_descriptions import escape_cpp_string_literal


def test_escape_cpp_string_literal_quotes() -> None:
    assert (
        escape_cpp_string_literal('isAnagram("anagram", "nagaram") should be true')
        == 'isAnagram(\\"anagram\\", \\"nagaram\\") should be true'
    )


def test_escape_cpp_string_literal_backslashes() -> None:
    assert escape_cpp_string_literal('path\\to\\file') == 'path\\\\to\\\\file'
