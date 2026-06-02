#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int max_profit(const std::vector<int>& prices);

TEST_CASE("maxProfit([1, 2]) should equal 1") {
    REQUIRE(max_profit(std::vector<int>{1, 2}) == 1);
}
