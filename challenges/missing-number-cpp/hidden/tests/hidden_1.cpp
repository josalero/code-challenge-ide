#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int missing_number(const std::vector<int>& nums);

TEST_CASE("missingNumber([9, 6, 4, 2, 3, 5, 7, 0, 1]) should equal 8") {
    REQUIRE(missing_number(std::vector<int>{9, 6, 4, 2, 3, 5, 7, 0, 1}) == 8);
}
