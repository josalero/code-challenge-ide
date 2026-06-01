package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.LinearSearch([]int{9}, 9) != 0 { t.Fatal("unexpected") }
		if solution.LinearSearch([]int{}, 1) != -1 { t.Fatal("unexpected") }
}
