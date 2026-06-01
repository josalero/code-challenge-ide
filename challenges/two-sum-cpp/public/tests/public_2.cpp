#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> two_sum(const std::vector<int>& nums, int target);

TEST_CASE("twoSum([3, 2, 4], 6) should return [1, 2]") {
    REQUIRE(two_sum(std::vector<int>{3, 2, 4}, 6) == std::vector<int>{1, 2});
}
