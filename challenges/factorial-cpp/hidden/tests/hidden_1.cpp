#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int factorial(int n);

TEST_CASE("factorial(10) should equal 3628800") {
    REQUIRE(factorial(10) == 3628800);
}
