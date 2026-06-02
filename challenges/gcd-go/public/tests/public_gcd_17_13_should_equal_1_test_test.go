package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicGcd1713ShouldEqual1(t *testing.T) {
	if solution.Gcd(17, 13) != 1 { t.Fatal("unexpected") }
}
