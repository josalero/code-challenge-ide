#include <catch2/catch_test_macros.hpp>
#include <vector>

extern bool is_power_of_two(int n);

TEST_CASE("isPowerOfTwo(3) should be false") {
    REQUIRE(is_power_of_two(3) == false);
}
