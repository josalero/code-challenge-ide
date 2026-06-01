package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicGcd5424ShouldEqual6(t *testing.T) {
	if solution.Gcd(54, 24) != 6 { t.Fatal("unexpected") }
}
