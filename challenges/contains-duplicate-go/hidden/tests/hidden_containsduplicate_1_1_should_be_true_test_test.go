package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenContainsduplicate11ShouldBeTrue(t *testing.T) {
	if solution.ContainsDuplicate([]int{1, 1}) != true { t.Fatal("unexpected") }
}
