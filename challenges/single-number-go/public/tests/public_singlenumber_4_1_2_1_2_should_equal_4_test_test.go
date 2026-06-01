package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicSinglenumber41212ShouldEqual4(t *testing.T) {
	if solution.SingleNumber([]int{4, 1, 2, 1, 2}) != 4 { t.Fatal("unexpected") }
}
