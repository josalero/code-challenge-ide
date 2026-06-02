#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern bool is_anagram(const std::string& s, const std::string& t);

TEST_CASE("isAnagram(\"ab\", \"a\") should be false") {
    REQUIRE(is_anagram("ab", "a") == false);
}
