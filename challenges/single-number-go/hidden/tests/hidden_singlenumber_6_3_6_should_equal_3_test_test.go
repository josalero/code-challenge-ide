package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenSinglenumber636ShouldEqual3(t *testing.T) {
	if solution.SingleNumber([]int{6, 3, 6}) != 3 { t.Fatal("unexpected") }
}
