#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern bool is_palindrome(const std::string& s);

TEST_CASE("isPalindrome(\"A man, a plan, a can…\") should be true") {
    REQUIRE(is_palindrome("A man, a plan, a canal: Panama") == true);
}
