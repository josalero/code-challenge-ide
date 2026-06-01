#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> merge_sorted(const std::vector<int>& a, const std::vector<int>& b);

TEST_CASE("mergeSorted([1], []) should return [1]") {
    REQUIRE(merge_sorted(std::vector<int>{1}, std::vector<int>{}) == std::vector<int>{1});
}
