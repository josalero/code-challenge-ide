package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIsprime1ShouldBeFalse(t *testing.T) {
	if solution.IsPrime(1) != false { t.Fatal("unexpected") }
}
