#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int linear_search(const std::vector<int>& nums, int target);

TEST_CASE("linear-search hidden") {
    REQUIRE(linear_search(std::vector<int>{9}, 9) == 0);
    REQUIRE(linear_search(std::vector<int>{}, 1) == -1);
}
