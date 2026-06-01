package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIsvalidShouldBeTrue(t *testing.T) {
	if solution.IsValidParentheses("{[]}") != true { t.Fatal("unexpected") }
}
