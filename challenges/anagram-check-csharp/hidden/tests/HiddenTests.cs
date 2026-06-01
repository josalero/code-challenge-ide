using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void IsanagramAAShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsAnagram("a", "a"));
    }

    [Fact]
    public void IsanagramAbAShouldBeFalse()
    {
        Assert.Equal(false, Solution.IsAnagram("ab", "a"));
    }
}
