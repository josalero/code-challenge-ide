package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.ClimbStairs(2) != 2 { t.Fatal("unexpected") }
		if solution.ClimbStairs(3) != 3 { t.Fatal("unexpected") }
}
