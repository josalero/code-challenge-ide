package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicLinearsearch125ShouldReturnIndex1(t *testing.T) {
	if solution.LinearSearch([]int{1, 2}, 5) != -1 { t.Fatal("unexpected") }
}
