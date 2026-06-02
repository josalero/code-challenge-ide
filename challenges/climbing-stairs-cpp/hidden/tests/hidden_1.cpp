#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int climb_stairs(int n);

TEST_CASE("climbStairs(10) should equal 89") {
    REQUIRE(climb_stairs(10) == 89);
}
