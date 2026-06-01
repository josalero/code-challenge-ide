package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIntegerSquareRootOf10ShouldBe3(t *testing.T) {
	if solution.MySqrt(10) != 3 { t.Fatal("unexpected") }
}
