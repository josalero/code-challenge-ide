#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int fib(int n);

TEST_CASE("fibonacci-number hidden") {
    REQUIRE(fib(10) == 55);
    REQUIRE(fib(6) == 8);
}
