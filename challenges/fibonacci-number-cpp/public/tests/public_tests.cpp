#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int fib(int n);

TEST_CASE("fibonacci-number public") {
    REQUIRE(fib(0) == 0);
    REQUIRE(fib(1) == 1);
    REQUIRE(fib(5) == 5);
}
