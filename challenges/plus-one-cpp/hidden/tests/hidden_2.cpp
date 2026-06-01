#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> plus_one(const std::vector<int>& digits);

TEST_CASE("plusOne([9, 9, 9]) should return [1, 0, 0, 0]") {
    REQUIRE(plus_one(std::vector<int>{9, 9, 9}) == std::vector<int>{1, 0, 0, 0});
}
