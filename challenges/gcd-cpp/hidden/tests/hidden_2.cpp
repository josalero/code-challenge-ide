#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int gcd(int a, int b);

TEST_CASE("GCD(0, 7) should equal 7") {
    REQUIRE(gcd(0, 7) == 7);
}
