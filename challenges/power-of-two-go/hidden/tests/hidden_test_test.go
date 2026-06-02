package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.IsPowerOfTwo(16) != true { t.Fatal("unexpected") }
		if solution.IsPowerOfTwo(0) != false { t.Fatal("unexpected") }
}
