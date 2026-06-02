package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicFactorial0ShouldEqual1(t *testing.T) {
	if solution.Factorial(0) != 1 { t.Fatal("unexpected") }
}
