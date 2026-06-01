#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int max_profit(const std::vector<int>& prices);

TEST_CASE("maxProfit([7, 6, 4, 3, 1]) should equal 0") {
    REQUIRE(max_profit(std::vector<int>{7, 6, 4, 3, 1}) == 0);
}
