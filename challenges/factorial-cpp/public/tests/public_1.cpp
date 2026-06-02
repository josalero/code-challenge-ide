#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int factorial(int n);

TEST_CASE("factorial(0) should equal 1") {
    REQUIRE(factorial(0) == 1);
}
