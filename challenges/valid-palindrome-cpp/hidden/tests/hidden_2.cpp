#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern bool is_palindrome(const std::string& s);

TEST_CASE("isPalindrome(" ") should be true") {
    REQUIRE(is_palindrome(" ") == true);
}
