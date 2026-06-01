using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void IspalindromeAManAPlanACanShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsPalindrome("A man, a plan, a canal: Panama"));
    }

    [Fact]
    public void IspalindromeRaceACarShouldBeFalse()
    {
        Assert.Equal(false, Solution.IsPalindrome("race a car"));
    }
}
