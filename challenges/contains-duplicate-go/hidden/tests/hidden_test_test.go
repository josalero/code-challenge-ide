package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.ContainsDuplicate([]int{1, 1}) != true { t.Fatal("unexpected") }
		if solution.ContainsDuplicate([]int{}) != false { t.Fatal("unexpected") }
}
