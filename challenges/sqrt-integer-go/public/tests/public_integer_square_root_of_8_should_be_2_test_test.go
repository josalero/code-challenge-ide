package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIntegerSquareRootOf8ShouldBe2(t *testing.T) {
	if solution.MySqrt(8) != 2 { t.Fatal("unexpected") }
}
