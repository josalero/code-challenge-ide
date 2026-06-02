package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicMaxprofit715364ShouldEqual5(t *testing.T) {
	if solution.MaxProfit([]int{7, 1, 5, 3, 6, 4}) != 5 { t.Fatal("unexpected") }
}
