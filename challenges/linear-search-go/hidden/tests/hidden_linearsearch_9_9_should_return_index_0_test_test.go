package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenLinearsearch99ShouldReturnIndex0(t *testing.T) {
	if solution.LinearSearch([]int{9}, 9) != 0 { t.Fatal("unexpected") }
}
