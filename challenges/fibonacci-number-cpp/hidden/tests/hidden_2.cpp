#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int fib(int n);

TEST_CASE("fibonacci(6) should equal 8") {
    REQUIRE(fib(6) == 8);
}
