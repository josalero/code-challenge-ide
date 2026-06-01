package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicPlusone123ShouldReturn124(t *testing.T) {
	got := solution.PlusOne([]int{1, 2, 3})
want := []int{1, 2, 4}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
