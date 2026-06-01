#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int gcd(int a, int b);

TEST_CASE("GCD(17, 13) should equal 1") {
    REQUIRE(gcd(17, 13) == 1);
}
