package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIsanagramAnagramNagaramShouldBeTrue(t *testing.T) {
	if solution.IsAnagram("anagram", "nagaram") != true { t.Fatal("unexpected") }
}
