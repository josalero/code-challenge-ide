#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> bubble_sort(const std::vector<int>& nums);

TEST_CASE("bubbleSort([5, 4, 3, 2, 1]) should return [1, 2, 3, 4, 5]") {
    REQUIRE(bubble_sort(std::vector<int>{5, 4, 3, 2, 1}) == std::vector<int>{1, 2, 3, 4, 5});
}
