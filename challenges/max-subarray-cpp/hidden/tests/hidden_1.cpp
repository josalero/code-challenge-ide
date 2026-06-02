#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int max_sub_array(const std::vector<int>& nums);

TEST_CASE("maxSubArray([5, 4, -1, 7, 8]) should equal 23") {
    REQUIRE(max_sub_array(std::vector<int>{5, 4, -1, 7, 8}) == 23);
}
