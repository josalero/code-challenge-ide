#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int binary_search(const std::vector<int>& nums, int target);

TEST_CASE("binarySearch([1, 3, 5, 7, 9], 4) should return index -1") {
    REQUIRE(binary_search(std::vector<int>{1, 3, 5, 7, 9}, 4) == -1);
}
