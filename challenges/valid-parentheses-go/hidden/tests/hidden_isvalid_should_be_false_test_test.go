package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIsvalidShouldBeFalse(t *testing.T) {
	if solution.IsValidParentheses("(]") != false { t.Fatal("unexpected") }
}
