#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int gcd(int a, int b);

TEST_CASE("gcd public") {
    REQUIRE(gcd(54, 24) == 6);
    REQUIRE(gcd(17, 13) == 1);
    REQUIRE(gcd(25, 15) == 5);
}
