#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> two_sum(const std::vector<int>& nums, int target);

TEST_CASE("twoSum([2, 7, 11, 15], 9) should return [0, 1]") {
    REQUIRE(two_sum(std::vector<int>{2, 7, 11, 15}, 9) == std::vector<int>{0, 1});
}
