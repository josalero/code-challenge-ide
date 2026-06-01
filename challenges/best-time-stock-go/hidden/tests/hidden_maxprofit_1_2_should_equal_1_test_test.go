package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenMaxprofit12ShouldEqual1(t *testing.T) {
	if solution.MaxProfit([]int{1, 2}) != 1 { t.Fatal("unexpected") }
}
