package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicBinarysearch135794ShouldReturnIn(t *testing.T) {
	if solution.BinarySearch([]int{1, 3, 5, 7, 9}, 4) != -1 { t.Fatal("unexpected") }
}
