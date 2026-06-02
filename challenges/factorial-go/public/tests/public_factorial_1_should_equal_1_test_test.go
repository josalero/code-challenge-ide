package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicFactorial1ShouldEqual1(t *testing.T) {
	if solution.Factorial(1) != 1 { t.Fatal("unexpected") }
}
