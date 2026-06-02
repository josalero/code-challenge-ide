package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenBubblesort54321ShouldReturn123(t *testing.T) {
	got := solution.BubbleSort([]int{5, 4, 3, 2, 1})
want := []int{1, 2, 3, 4, 5}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
