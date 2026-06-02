package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIspoweroftwo3ShouldBeFalse(t *testing.T) {
	if solution.IsPowerOfTwo(3) != false { t.Fatal("unexpected") }
}
