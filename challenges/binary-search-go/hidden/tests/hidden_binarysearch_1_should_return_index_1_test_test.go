package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenBinarysearch1ShouldReturnIndex1(t *testing.T) {
	if solution.BinarySearch([]int{}, 1) != -1 { t.Fatal("unexpected") }
}
