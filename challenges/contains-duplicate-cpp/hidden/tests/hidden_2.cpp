#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool contains_duplicate(const std::vector<int>& nums);

TEST_CASE("containsDuplicate([]) should be false") {
    REQUIRE(contains_duplicate(std::vector<int>{}) == false);
}
