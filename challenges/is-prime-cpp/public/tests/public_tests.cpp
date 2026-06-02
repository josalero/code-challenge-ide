#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool is_prime(int n);

TEST_CASE("is-prime public") {
    REQUIRE(is_prime(1) == false);
    REQUIRE(is_prime(2) == true);
    REQUIRE(is_prime(17) == true);
}
