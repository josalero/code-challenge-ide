package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIsanagramAAShouldBeTrue(t *testing.T) {
	if solution.IsAnagram("a", "a") != true { t.Fatal("unexpected") }
}
