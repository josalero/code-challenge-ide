package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenMissingnumber964235701ShouldEq(t *testing.T) {
	if solution.MissingNumber([]int{9, 6, 4, 2, 3, 5, 7, 0, 1}) != 8 { t.Fatal("unexpected") }
}
