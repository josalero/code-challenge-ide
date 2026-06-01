package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenFactorial3ShouldEqual6(t *testing.T) {
	if solution.Factorial(3) != 6 { t.Fatal("unexpected") }
}
