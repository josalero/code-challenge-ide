package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenClimbstairs1ShouldEqual1(t *testing.T) {
	if solution.ClimbStairs(1) != 1 { t.Fatal("unexpected") }
}
