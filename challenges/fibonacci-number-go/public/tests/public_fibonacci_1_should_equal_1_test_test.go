package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicFibonacci1ShouldEqual1(t *testing.T) {
	if solution.Fib(1) != 1 { t.Fatal("unexpected") }
}
