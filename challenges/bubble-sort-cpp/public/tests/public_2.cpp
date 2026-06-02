#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> bubble_sort(const std::vector<int>& nums);

TEST_CASE("bubbleSort([1]) should return [1]") {
    REQUIRE(bubble_sort(std::vector<int>{1}) == std::vector<int>{1});
}
