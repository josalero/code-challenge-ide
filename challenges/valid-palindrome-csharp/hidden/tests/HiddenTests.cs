using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void IspalindromeShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsPalindrome(""));
    }

    [Fact]
    public void IspalindromeShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsPalindrome(" "));
    }
}
