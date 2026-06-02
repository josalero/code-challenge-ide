package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenContainsduplicateShouldBeFalse(t *testing.T) {
	if solution.ContainsDuplicate([]int{}) != false { t.Fatal("unexpected") }
}
