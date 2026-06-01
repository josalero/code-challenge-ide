using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void IsvalidShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsValidParentheses("()"));
    }

    [Fact]
    public void IsvalidShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsValidParentheses("()[]{}"));
    }
}
