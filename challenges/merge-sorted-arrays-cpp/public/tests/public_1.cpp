#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::vector<int> merge_sorted(const std::vector<int>& a, const std::vector<int>& b);

TEST_CASE("mergeSorted([1, 2, 3], [2, 5, 6]) should return [1, 2, 2, 3, 5, 6]") {
    REQUIRE(merge_sorted(std::vector<int>{1, 2, 3}, std::vector<int>{2, 5, 6}) == std::vector<int>{1, 2, 2, 3, 5, 6});
}
