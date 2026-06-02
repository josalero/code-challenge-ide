#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int binary_search(const std::vector<int>& nums, int target);

TEST_CASE("binarySearch([2, 4, 6], 2) should return index 0") {
    REQUIRE(binary_search(std::vector<int>{2, 4, 6}, 2) == 0);
}
