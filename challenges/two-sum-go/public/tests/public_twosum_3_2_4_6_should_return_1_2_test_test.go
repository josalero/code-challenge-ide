package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicTwosum3246ShouldReturn12(t *testing.T) {
	got := solution.TwoSum([]int{3, 2, 4}, 6)
want := []int{1, 2}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
