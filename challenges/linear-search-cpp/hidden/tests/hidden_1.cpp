#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int linear_search(const std::vector<int>& nums, int target);

TEST_CASE("linearSearch([9], 9) should return index 0") {
    REQUIRE(linear_search(std::vector<int>{9}, 9) == 0);
}
