package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenBubblesortShouldReturn(t *testing.T) {
	got := solution.BubbleSort([]int{})
want := []int{}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
