#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool is_power_of_two(int n);

TEST_CASE("power-of-two hidden") {
    REQUIRE(is_power_of_two(16) == true);
    REQUIRE(is_power_of_two(0) == false);
}
