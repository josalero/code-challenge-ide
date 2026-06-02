package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenReversestringAShouldBeA(t *testing.T) {
	if solution.ReverseString("a") != "a" { t.Fatal("unexpected") }
}
