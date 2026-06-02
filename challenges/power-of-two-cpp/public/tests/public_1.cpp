#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool is_power_of_two(int n);

TEST_CASE("isPowerOfTwo(1) should be true") {
    REQUIRE(is_power_of_two(1) == true);
}
