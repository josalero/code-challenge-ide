#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool is_power_of_two(int n);

TEST_CASE("power-of-two public") {
    REQUIRE(is_power_of_two(1) == true);
    REQUIRE(is_power_of_two(3) == false);
}
