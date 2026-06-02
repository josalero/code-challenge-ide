#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int linear_search(const std::vector<int>& nums, int target);

TEST_CASE("linear-search public") {
    REQUIRE(linear_search(std::vector<int>{2, 3, 4}, 3) == 1);
    REQUIRE(linear_search(std::vector<int>{1, 2}, 5) == -1);
}
