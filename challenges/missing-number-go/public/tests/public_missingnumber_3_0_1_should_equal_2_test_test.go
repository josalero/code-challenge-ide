package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicMissingnumber301ShouldEqual2(t *testing.T) {
	if solution.MissingNumber([]int{3, 0, 1}) != 2 { t.Fatal("unexpected") }
}
