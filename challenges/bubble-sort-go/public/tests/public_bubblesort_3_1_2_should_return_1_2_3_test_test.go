package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicBubblesort312ShouldReturn123(t *testing.T) {
	got := solution.BubbleSort([]int{3, 1, 2})
want := []int{1, 2, 3}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
