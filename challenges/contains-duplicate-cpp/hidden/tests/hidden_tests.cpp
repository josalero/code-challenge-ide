#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool contains_duplicate(const std::vector<int>& nums);

TEST_CASE("contains-duplicate hidden") {
    REQUIRE(contains_duplicate(std::vector<int>{1, 1}) == true);
    REQUIRE(contains_duplicate(std::vector<int>{}) == false);
}
