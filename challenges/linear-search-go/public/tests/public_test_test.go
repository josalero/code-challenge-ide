package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.LinearSearch([]int{2, 3, 4}, 3) != 1 { t.Fatal("unexpected") }
		if solution.LinearSearch([]int{1, 2}, 5) != -1 { t.Fatal("unexpected") }
}
