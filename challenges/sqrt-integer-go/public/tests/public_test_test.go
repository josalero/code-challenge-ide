package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.MySqrt(8) != 2 { t.Fatal("unexpected") }
		if solution.MySqrt(0) != 0 { t.Fatal("unexpected") }
}
