#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool contains_duplicate(const std::vector<int>& nums);

TEST_CASE("contains-duplicate public") {
    REQUIRE(contains_duplicate(std::vector<int>{1, 2, 3, 1}) == true);
    REQUIRE(contains_duplicate(std::vector<int>{1, 2, 3, 4}) == false);
}
