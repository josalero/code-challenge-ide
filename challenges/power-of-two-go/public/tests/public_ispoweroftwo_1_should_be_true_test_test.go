package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIspoweroftwo1ShouldBeTrue(t *testing.T) {
	if solution.IsPowerOfTwo(1) != true { t.Fatal("unexpected") }
}
