package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicMaxprofit76431ShouldEqual0(t *testing.T) {
	if solution.MaxProfit([]int{7, 6, 4, 3, 1}) != 0 { t.Fatal("unexpected") }
}
