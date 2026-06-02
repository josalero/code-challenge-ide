#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern bool is_anagram(const std::string& s, const std::string& t);

TEST_CASE("isAnagram(\"rat\", \"car\") should be false") {
    REQUIRE(is_anagram("rat", "car") == false);
}
