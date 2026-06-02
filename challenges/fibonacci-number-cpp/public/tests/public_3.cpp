#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int fib(int n);

TEST_CASE("fibonacci(5) should equal 5") {
    REQUIRE(fib(5) == 5);
}
