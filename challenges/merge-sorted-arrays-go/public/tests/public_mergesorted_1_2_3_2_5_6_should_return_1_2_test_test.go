package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicMergesorted123256ShouldReturn12(t *testing.T) {
	got := solution.MergeSorted([]int{1, 2, 3}, []int{2, 5, 6})
want := []int{1, 2, 2, 3, 5, 6}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
