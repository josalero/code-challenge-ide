package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIntegerSquareRootOf0ShouldBe0(t *testing.T) {
	if solution.MySqrt(0) != 0 { t.Fatal("unexpected") }
}
