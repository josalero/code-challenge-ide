#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int max_sub_array(const std::vector<int>& nums);

TEST_CASE("maxSubArray([-1]) should equal -1") {
    REQUIRE(max_sub_array(std::vector<int>{-1}) == -1);
}
