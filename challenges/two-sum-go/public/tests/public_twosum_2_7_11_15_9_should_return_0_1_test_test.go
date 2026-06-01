package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicTwosum2711159ShouldReturn01(t *testing.T) {
	got := solution.TwoSum([]int{2, 7, 11, 15}, 9)
want := []int{0, 1}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
