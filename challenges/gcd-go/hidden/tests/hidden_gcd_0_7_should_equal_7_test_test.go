package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenGcd07ShouldEqual7(t *testing.T) {
	if solution.Gcd(0, 7) != 7 { t.Fatal("unexpected") }
}
