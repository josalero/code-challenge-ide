#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int gcd(int a, int b);

TEST_CASE("gcd hidden") {
    REQUIRE(gcd(48, 18) == 12);
    REQUIRE(gcd(0, 7) == 7);
}
