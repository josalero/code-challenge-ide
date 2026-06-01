package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.MissingNumber([]int{3, 0, 1}) != 2 { t.Fatal("unexpected") }
		if solution.MissingNumber([]int{0}) != 1 { t.Fatal("unexpected") }
}
