package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenFactorial10ShouldEqual3628800(t *testing.T) {
	if solution.Factorial(10) != 3628800 { t.Fatal("unexpected") }
}
