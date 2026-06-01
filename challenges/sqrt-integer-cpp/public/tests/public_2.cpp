#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int my_sqrt(int x);

TEST_CASE("integer square root of 0 should be 0") {
    REQUIRE(my_sqrt(0) == 0);
}
