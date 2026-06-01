#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int climb_stairs(int n);

TEST_CASE("climbing-stairs hidden") {
    REQUIRE(climb_stairs(10) == 89);
    REQUIRE(climb_stairs(1) == 1);
}
