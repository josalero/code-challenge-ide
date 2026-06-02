#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int gcd(int a, int b);

TEST_CASE("GCD(54, 24) should equal 6") {
    REQUIRE(gcd(54, 24) == 6);
}
