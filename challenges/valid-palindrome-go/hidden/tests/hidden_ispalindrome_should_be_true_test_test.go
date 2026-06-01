package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIspalindromeShouldBeTrue(t *testing.T) {
	if solution.IsPalindrome(" ") != true { t.Fatal("unexpected") }
}
