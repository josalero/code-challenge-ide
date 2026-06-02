package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenTwosum1235ShouldReturn12(t *testing.T) {
	got := solution.TwoSum([]int{-1, -2, -3}, -5)
want := []int{1, 2}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
