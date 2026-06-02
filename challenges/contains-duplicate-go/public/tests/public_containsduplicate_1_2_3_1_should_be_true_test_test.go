package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicContainsduplicate1231ShouldBeTrue(t *testing.T) {
	if solution.ContainsDuplicate([]int{1, 2, 3, 1}) != true { t.Fatal("unexpected") }
}
