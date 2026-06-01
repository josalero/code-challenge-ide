#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int my_sqrt(int x);

TEST_CASE("integer square root of 2147483647 should be 46340") {
    REQUIRE(my_sqrt(2147483647) == 46340);
}
