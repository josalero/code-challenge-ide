package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenLinearsearch1ShouldReturnIndex1(t *testing.T) {
	if solution.LinearSearch([]int{}, 1) != -1 { t.Fatal("unexpected") }
}
