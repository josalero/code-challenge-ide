#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern bool is_anagram(const std::string& s, const std::string& t);

TEST_CASE("isAnagram(\"anagram\", \"nagaram\") should be true") {
    REQUIRE(is_anagram("anagram", "nagaram") == true);
}
