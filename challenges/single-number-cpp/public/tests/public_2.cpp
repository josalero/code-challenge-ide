#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int single_number(const std::vector<int>& nums);

TEST_CASE("singleNumber([4, 1, 2, 1, 2]) should equal 4") {
    REQUIRE(single_number(std::vector<int>{4, 1, 2, 1, 2}) == 4);
}
