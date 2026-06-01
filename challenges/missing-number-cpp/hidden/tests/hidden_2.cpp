#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int missing_number(const std::vector<int>& nums);

TEST_CASE("missingNumber([1]) should equal 0") {
    REQUIRE(missing_number(std::vector<int>{1}) == 0);
}
