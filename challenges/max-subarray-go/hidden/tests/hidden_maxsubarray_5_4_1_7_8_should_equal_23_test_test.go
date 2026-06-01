package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenMaxsubarray54178ShouldEqual23(t *testing.T) {
	if solution.MaxSubArray([]int{5, 4, -1, 7, 8}) != 23 { t.Fatal("unexpected") }
}
