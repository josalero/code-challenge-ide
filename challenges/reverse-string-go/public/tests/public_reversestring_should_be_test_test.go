package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicReversestringShouldBe(t *testing.T) {
	if solution.ReverseString("") != "" { t.Fatal("unexpected") }
}
