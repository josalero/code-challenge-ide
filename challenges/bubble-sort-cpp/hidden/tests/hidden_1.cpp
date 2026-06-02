#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> bubble_sort(const std::vector<int>& nums);

TEST_CASE("bubbleSort([]) should return []") {
    REQUIRE(bubble_sort(std::vector<int>{}) == std::vector<int>{});
}
