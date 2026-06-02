package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicClimbstairs3ShouldEqual3(t *testing.T) {
	if solution.ClimbStairs(3) != 3 { t.Fatal("unexpected") }
}
