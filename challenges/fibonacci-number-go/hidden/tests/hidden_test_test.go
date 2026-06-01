package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.Fib(10) != 55 { t.Fatal("unexpected") }
		if solution.Fib(6) != 8 { t.Fatal("unexpected") }
}
