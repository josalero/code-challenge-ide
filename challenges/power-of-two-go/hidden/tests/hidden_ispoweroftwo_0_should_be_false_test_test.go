package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIspoweroftwo0ShouldBeFalse(t *testing.T) {
	if solution.IsPowerOfTwo(0) != false { t.Fatal("unexpected") }
}
