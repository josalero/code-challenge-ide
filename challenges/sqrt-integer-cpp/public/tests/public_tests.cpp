#include <catch2/catch_test_macros.hpp>
#include <vector>

extern int my_sqrt(int x);

TEST_CASE("sqrt-integer public") {
    REQUIRE(my_sqrt(8) == 2);
    REQUIRE(my_sqrt(0) == 0);
}
