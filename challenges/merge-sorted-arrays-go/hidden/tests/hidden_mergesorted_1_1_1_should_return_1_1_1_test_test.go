package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenMergesorted111ShouldReturn111(t *testing.T) {
	got := solution.MergeSorted([]int{1, 1}, []int{1})
want := []int{1, 1, 1}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
