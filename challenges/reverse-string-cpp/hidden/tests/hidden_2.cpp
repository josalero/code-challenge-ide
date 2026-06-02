#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern std::string reverse_string(const std::string& s);

TEST_CASE("reverseString("race") should be "ecar"") {
    REQUIRE(reverse_string("race") == "ecar");
}
