#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int gcd(int a, int b);

TEST_CASE("GCD(48, 18) should equal 12") {
    REQUIRE(gcd(48, 18) == 12);
}
