package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicBubblesort1ShouldReturn1(t *testing.T) {
	got := solution.BubbleSort([]int{1})
want := []int{1}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
