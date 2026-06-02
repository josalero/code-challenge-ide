package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIspoweroftwo16ShouldBeTrue(t *testing.T) {
	if solution.IsPowerOfTwo(16) != true { t.Fatal("unexpected") }
}
