package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestGcdZero(t *testing.T) {
	if solution.Gcd(0, 7) != 7 {
		t.Fatalf("expected 7")
	}
}

func TestGcdEqual(t *testing.T) {
	if solution.Gcd(12, 12) != 12 {
		t.Fatalf("expected 12")
	}
}
