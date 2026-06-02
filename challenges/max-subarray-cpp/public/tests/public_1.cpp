#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int max_sub_array(const std::vector<int>& nums);

TEST_CASE("maxSubArray([-2, 1, -3, 4, -1, 2, 1, -4, 3]) should equal 6") {
    REQUIRE(max_sub_array(std::vector<int>{-2, 1, -3, 4, -1, 2, 1, -4, 3}) == 6);
}
