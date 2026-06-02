package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.Factorial(10) != 3628800 { t.Fatal("unexpected") }
		if solution.Factorial(3) != 6 { t.Fatal("unexpected") }
}
