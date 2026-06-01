package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.Fib(0) != 0 { t.Fatal("unexpected") }
		if solution.Fib(1) != 1 { t.Fatal("unexpected") }
		if solution.Fib(5) != 5 { t.Fatal("unexpected") }
}
