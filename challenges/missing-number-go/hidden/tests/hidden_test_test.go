package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.MissingNumber([]int{9, 6, 4, 2, 3, 5, 7, 0, 1}) != 8 { t.Fatal("unexpected") }
		if solution.MissingNumber([]int{1}) != 0 { t.Fatal("unexpected") }
}
