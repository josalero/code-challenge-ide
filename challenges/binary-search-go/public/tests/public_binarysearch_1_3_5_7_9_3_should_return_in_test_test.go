package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicBinarysearch135793ShouldReturnIn(t *testing.T) {
	if solution.BinarySearch([]int{1, 3, 5, 7, 9}, 3) != 1 { t.Fatal("unexpected") }
}
