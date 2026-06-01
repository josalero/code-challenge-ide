#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int single_number(const std::vector<int>& nums);

TEST_CASE("singleNumber([1]) should equal 1") {
    REQUIRE(single_number(std::vector<int>{1}) == 1);
}
