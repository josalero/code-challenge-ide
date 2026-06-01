package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIsvalidShouldBeTrue(t *testing.T) {
	if solution.IsValidParentheses("()[]{}") != true { t.Fatal("unexpected") }
}
