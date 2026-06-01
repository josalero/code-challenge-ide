package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIspalindromeRaceACarShouldBeFalse(t *testing.T) {
	if solution.IsPalindrome("race a car") != false { t.Fatal("unexpected") }
}
