package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicReversestringHelloShouldBeOlleh(t *testing.T) {
	if solution.ReverseString("hello") != "olleh" { t.Fatal("unexpected") }
}
