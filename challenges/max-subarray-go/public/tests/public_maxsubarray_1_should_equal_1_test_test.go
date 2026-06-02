package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicMaxsubarray1ShouldEqual1(t *testing.T) {
	if solution.MaxSubArray([]int{1}) != 1 { t.Fatal("unexpected") }
}
