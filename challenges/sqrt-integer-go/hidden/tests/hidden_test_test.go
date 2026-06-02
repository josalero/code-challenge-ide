package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.MySqrt(10) != 3 { t.Fatal("unexpected") }
		if solution.MySqrt(2147483647) != 46340 { t.Fatal("unexpected") }
}
