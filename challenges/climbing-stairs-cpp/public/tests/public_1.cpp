#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int climb_stairs(int n);

TEST_CASE("climbStairs(2) should equal 2") {
    REQUIRE(climb_stairs(2) == 2);
}
