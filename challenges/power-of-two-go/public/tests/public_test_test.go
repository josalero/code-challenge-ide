package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.IsPowerOfTwo(1) != true { t.Fatal("unexpected") }
		if solution.IsPowerOfTwo(3) != false { t.Fatal("unexpected") }
}
