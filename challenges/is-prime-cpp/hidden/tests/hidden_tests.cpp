#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool is_prime(int n);

TEST_CASE("is-prime hidden") {
    REQUIRE(is_prime(15) == false);
    REQUIRE(is_prime(97) == true);
}
