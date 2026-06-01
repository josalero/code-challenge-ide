package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicSinglenumber221ShouldEqual1(t *testing.T) {
	if solution.SingleNumber([]int{2, 2, 1}) != 1 { t.Fatal("unexpected") }
}
