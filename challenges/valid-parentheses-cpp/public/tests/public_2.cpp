#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern bool is_valid_parentheses(const std::string& s);

TEST_CASE("isValid(\"()[]{}\") should be true") {
    REQUIRE(is_valid_parentheses("()[]{}") == true);
}
