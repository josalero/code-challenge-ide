package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIsprime15ShouldBeFalse(t *testing.T) {
	if solution.IsPrime(15) != false { t.Fatal("unexpected") }
}
