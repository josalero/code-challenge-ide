package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIntegerSquareRootOf2147483647Should(t *testing.T) {
	if solution.MySqrt(2147483647) != 46340 { t.Fatal("unexpected") }
}
