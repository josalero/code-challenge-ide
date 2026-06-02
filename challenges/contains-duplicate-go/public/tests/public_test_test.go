package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.ContainsDuplicate([]int{1, 2, 3, 1}) != true { t.Fatal("unexpected") }
		if solution.ContainsDuplicate([]int{1, 2, 3, 4}) != false { t.Fatal("unexpected") }
}
