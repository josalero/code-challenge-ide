package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenMaxprofit241ShouldEqual2(t *testing.T) {
	if solution.MaxProfit([]int{2, 4, 1}) != 2 { t.Fatal("unexpected") }
}
