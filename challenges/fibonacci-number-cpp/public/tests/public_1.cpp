#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int fib(int n);

TEST_CASE("fibonacci(0) should equal 0") {
    REQUIRE(fib(0) == 0);
}
