#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int fib(int n);

TEST_CASE("fibonacci(1) should equal 1") {
    REQUIRE(fib(1) == 1);
}
