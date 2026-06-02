#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int single_number(const std::vector<int>& nums);

TEST_CASE("singleNumber([6, 3, 6]) should equal 3") {
    REQUIRE(single_number(std::vector<int>{6, 3, 6}) == 3);
}
