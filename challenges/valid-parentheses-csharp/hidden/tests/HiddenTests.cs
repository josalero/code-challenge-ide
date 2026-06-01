using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void IsvalidShouldBeFalse()
    {
        Assert.Equal(false, Solution.IsValidParentheses("(]"));
    }

    [Fact]
    public void IsvalidShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsValidParentheses("{[]}"));
    }
}
