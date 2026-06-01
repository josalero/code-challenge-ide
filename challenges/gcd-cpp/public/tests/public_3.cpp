#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int gcd(int a, int b);

TEST_CASE("GCD(25, 15) should equal 5") {
    REQUIRE(gcd(25, 15) == 5);
}
