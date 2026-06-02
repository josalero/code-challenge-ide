package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicFibonacci0ShouldEqual0(t *testing.T) {
	if solution.Fib(0) != 0 { t.Fatal("unexpected") }
}
