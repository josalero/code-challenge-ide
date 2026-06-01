package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenMissingnumber1ShouldEqual0(t *testing.T) {
	if solution.MissingNumber([]int{1}) != 0 { t.Fatal("unexpected") }
}
