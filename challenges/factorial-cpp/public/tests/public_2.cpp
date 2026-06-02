#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int factorial(int n);

TEST_CASE("factorial(5) should equal 120") {
    REQUIRE(factorial(5) == 120);
}
