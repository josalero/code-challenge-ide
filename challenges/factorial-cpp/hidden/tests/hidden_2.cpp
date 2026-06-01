#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int factorial(int n);

TEST_CASE("factorial(3) should equal 6") {
    REQUIRE(factorial(3) == 6);
}
