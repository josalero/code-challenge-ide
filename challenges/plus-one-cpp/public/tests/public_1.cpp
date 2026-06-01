#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> plus_one(const std::vector<int>& digits);

TEST_CASE("plusOne([1, 2, 3]) should return [1, 2, 4]") {
    REQUIRE(plus_one(std::vector<int>{1, 2, 3}) == std::vector<int>{1, 2, 4});
}
