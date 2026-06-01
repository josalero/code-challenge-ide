package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicClimbstairs2ShouldEqual2(t *testing.T) {
	if solution.ClimbStairs(2) != 2 { t.Fatal("unexpected") }
}
