#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int linear_search(const std::vector<int>& nums, int target);

TEST_CASE("linearSearch([2, 3, 4], 3) should return index 1") {
    REQUIRE(linear_search(std::vector<int>{2, 3, 4}, 3) == 1);
}
