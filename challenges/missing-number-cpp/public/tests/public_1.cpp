#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int missing_number(const std::vector<int>& nums);

TEST_CASE("missingNumber([3, 0, 1]) should equal 2") {
    REQUIRE(missing_number(std::vector<int>{3, 0, 1}) == 2);
}
