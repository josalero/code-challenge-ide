package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestGcdSample(t *testing.T) {
	if solution.Gcd(54, 24) != 6 {
		t.Fatalf("expected 6")
	}
}

func TestGcdCoprime(t *testing.T) {
	if solution.Gcd(17, 13) != 1 {
		t.Fatalf("expected 1")
	}
}
