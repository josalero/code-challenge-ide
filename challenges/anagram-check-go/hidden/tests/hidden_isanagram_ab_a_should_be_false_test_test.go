package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHiddenIsanagramAbAShouldBeFalse(t *testing.T) {
	if solution.IsAnagram("ab", "a") != false { t.Fatal("unexpected") }
}
