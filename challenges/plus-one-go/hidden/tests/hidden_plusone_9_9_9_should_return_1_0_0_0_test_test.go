package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenPlusone999ShouldReturn1000(t *testing.T) {
	got := solution.PlusOne([]int{9, 9, 9})
want := []int{1, 0, 0, 0}
if len(got) != len(want) { t.Fatal("length") }
for i := range want { if got[i] != want[i] { t.Fatal("unexpected") } }
}
