package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIsprime17ShouldBeTrue(t *testing.T) {
	if solution.IsPrime(17) != true { t.Fatal("unexpected") }
}
