package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenFibonacci10ShouldEqual55(t *testing.T) {
	if solution.Fib(10) != 55 { t.Fatal("unexpected") }
}
