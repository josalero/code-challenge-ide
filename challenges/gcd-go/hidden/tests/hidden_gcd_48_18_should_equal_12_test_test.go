package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenGcd4818ShouldEqual12(t *testing.T) {
	if solution.Gcd(48, 18) != 12 { t.Fatal("unexpected") }
}
