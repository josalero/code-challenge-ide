#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int my_sqrt(int x);

TEST_CASE("integer square root of 8 should be 2") {
    REQUIRE(my_sqrt(8) == 2);
}
