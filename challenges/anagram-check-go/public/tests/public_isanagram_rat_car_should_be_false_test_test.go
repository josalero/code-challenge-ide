package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIsanagramRatCarShouldBeFalse(t *testing.T) {
	if solution.IsAnagram("rat", "car") != false { t.Fatal("unexpected") }
}
