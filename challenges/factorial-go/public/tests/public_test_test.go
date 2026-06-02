package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.Factorial(0) != 1 { t.Fatal("unexpected") }
		if solution.Factorial(5) != 120 { t.Fatal("unexpected") }
		if solution.Factorial(1) != 1 { t.Fatal("unexpected") }
}
