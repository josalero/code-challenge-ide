package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenClimbstairs10ShouldEqual89(t *testing.T) {
	if solution.ClimbStairs(10) != 89 { t.Fatal("unexpected") }
}
