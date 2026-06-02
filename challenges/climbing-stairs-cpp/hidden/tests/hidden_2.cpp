#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int climb_stairs(int n);

TEST_CASE("climbStairs(1) should equal 1") {
    REQUIRE(climb_stairs(1) == 1);
}
