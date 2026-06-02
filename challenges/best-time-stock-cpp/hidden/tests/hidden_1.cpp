#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern int max_profit(const std::vector<int>& prices);

TEST_CASE("maxProfit([2, 4, 1]) should equal 2") {
    REQUIRE(max_profit(std::vector<int>{2, 4, 1}) == 2);
}
