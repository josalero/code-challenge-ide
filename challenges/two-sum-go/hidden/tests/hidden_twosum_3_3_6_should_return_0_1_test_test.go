package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenTwosum336ShouldReturn01(t *testing.T) {
	got := solution.TwoSum([]int{3, 3}, 6)
want := []int{0, 1}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
