#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int factorial(int n);

TEST_CASE("factorial public") {
    REQUIRE(factorial(0) == 1);
    REQUIRE(factorial(5) == 120);
    REQUIRE(factorial(1) == 1);
}
