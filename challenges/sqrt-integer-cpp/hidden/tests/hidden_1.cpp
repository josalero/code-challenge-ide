#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int my_sqrt(int x);

TEST_CASE("integer square root of 10 should be 3") {
    REQUIRE(my_sqrt(10) == 3);
}
