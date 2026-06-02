#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int climb_stairs(int n);

TEST_CASE("climbing-stairs public") {
    REQUIRE(climb_stairs(2) == 2);
    REQUIRE(climb_stairs(3) == 3);
}
