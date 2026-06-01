#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool contains_duplicate(const std::vector<int>& nums);

TEST_CASE("containsDuplicate([1, 2, 3, 4]) should be false") {
    REQUIRE(contains_duplicate(std::vector<int>{1, 2, 3, 4}) == false);
}
