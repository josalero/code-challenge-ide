package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicMaxsubarray213412143ShouldEqua(t *testing.T) {
	if solution.MaxSubArray([]int{-2, 1, -3, 4, -1, 2, 1, -4, 3}) != 6 { t.Fatal("unexpected") }
}
