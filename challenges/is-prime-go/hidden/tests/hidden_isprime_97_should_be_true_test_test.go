package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIsprime97ShouldBeTrue(t *testing.T) {
	if solution.IsPrime(97) != true { t.Fatal("unexpected") }
}
