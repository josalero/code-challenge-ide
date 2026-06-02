#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> two_sum(const std::vector<int>& nums, int target);

TEST_CASE("twoSum([3, 3], 6) should return [0, 1]") {
    REQUIRE(two_sum(std::vector<int>{3, 3}, 6) == std::vector<int>{0, 1});
}
