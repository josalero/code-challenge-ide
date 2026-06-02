package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicMissingnumber0ShouldEqual1(t *testing.T) {
	if solution.MissingNumber([]int{0}) != 1 { t.Fatal("unexpected") }
}
