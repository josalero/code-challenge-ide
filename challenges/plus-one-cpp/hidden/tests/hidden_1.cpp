#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> plus_one(const std::vector<int>& digits);

TEST_CASE("plusOne([0]) should return [1]") {
    REQUIRE(plus_one(std::vector<int>{0}) == std::vector<int>{1});
}
