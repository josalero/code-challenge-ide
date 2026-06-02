#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int fib(int n);

TEST_CASE("fibonacci(10) should equal 55") {
    REQUIRE(fib(10) == 55);
}
