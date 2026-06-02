#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool is_prime(int n);

TEST_CASE("isPrime(2) should be true") {
    REQUIRE(is_prime(2) == true);
}
