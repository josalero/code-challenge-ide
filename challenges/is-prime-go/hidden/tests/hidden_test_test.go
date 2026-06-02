package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.IsPrime(15) != false { t.Fatal("unexpected") }
		if solution.IsPrime(97) != true { t.Fatal("unexpected") }
}
