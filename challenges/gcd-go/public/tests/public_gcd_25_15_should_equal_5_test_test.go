package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicGcd2515ShouldEqual5(t *testing.T) {
	if solution.Gcd(25, 15) != 5 { t.Fatal("unexpected") }
}
