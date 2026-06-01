#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool is_prime(int n);

TEST_CASE("isPrime(1) should be false") {
    REQUIRE(is_prime(1) == false);
}
