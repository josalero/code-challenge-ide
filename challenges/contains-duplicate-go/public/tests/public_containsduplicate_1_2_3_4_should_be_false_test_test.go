package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicContainsduplicate1234ShouldBeFalse(t *testing.T) {
	if solution.ContainsDuplicate([]int{1, 2, 3, 4}) != false { t.Fatal("unexpected") }
}
