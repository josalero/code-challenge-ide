package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublic(t *testing.T) {
	if solution.IsPrime(1) != false { t.Fatal("unexpected") }
		if solution.IsPrime(2) != true { t.Fatal("unexpected") }
		if solution.IsPrime(17) != true { t.Fatal("unexpected") }
}
