package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenBinarysearch2462ShouldReturnIndex(t *testing.T) {
	if solution.BinarySearch([]int{2, 4, 6}, 2) != 0 { t.Fatal("unexpected") }
}
