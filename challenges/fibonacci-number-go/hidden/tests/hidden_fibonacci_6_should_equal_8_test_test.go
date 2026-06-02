package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenFibonacci6ShouldEqual8(t *testing.T) {
	if solution.Fib(6) != 8 { t.Fatal("unexpected") }
}
