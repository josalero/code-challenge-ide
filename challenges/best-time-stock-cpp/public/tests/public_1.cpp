#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int max_profit(const std::vector<int>& prices);

TEST_CASE("maxProfit([7, 1, 5, 3, 6, 4]) should equal 5") {
    REQUIRE(max_profit(std::vector<int>{7, 1, 5, 3, 6, 4}) == 5);
}
