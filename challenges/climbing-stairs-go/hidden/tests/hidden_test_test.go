package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.ClimbStairs(10) != 89 { t.Fatal("unexpected") }
		if solution.ClimbStairs(1) != 1 { t.Fatal("unexpected") }
}
