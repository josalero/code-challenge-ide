package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIsprime2ShouldBeTrue(t *testing.T) {
	if solution.IsPrime(2) != true { t.Fatal("unexpected") }
}
