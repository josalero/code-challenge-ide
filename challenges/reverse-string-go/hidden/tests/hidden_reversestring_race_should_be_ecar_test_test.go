package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenReversestringRaceShouldBeEcar(t *testing.T) {
	if solution.ReverseString("race") != "ecar" { t.Fatal("unexpected") }
}
