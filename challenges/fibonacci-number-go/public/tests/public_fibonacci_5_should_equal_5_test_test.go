package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicFibonacci5ShouldEqual5(t *testing.T) {
	if solution.Fib(5) != 5 { t.Fatal("unexpected") }
}
