package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenSinglenumber1ShouldEqual1(t *testing.T) {
	if solution.SingleNumber([]int{1}) != 1 { t.Fatal("unexpected") }
}
