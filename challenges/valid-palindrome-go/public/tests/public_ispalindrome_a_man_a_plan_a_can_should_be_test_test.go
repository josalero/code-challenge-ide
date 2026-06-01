package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestPublicIspalindromeAManAPlanACanShouldBe(t *testing.T) {
	if solution.IsPalindrome("A man, a plan, a canal: Panama") != true { t.Fatal("unexpected") }
}
