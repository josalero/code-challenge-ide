#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int factorial(int n);

TEST_CASE("factorial(1) should equal 1") {
    REQUIRE(factorial(1) == 1);
}
