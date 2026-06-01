package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicFactorial5ShouldEqual120(t *testing.T) {
	if solution.Factorial(5) != 120 { t.Fatal("unexpected") }
}
