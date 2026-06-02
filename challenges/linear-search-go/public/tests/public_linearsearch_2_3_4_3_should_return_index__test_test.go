package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicLinearsearch2343ShouldReturnIndex(t *testing.T) {
	if solution.LinearSearch([]int{2, 3, 4}, 3) != 1 { t.Fatal("unexpected") }
}
