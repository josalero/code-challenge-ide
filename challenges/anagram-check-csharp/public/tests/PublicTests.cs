using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void IsanagramAnagramNagaramShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsAnagram("anagram", "nagaram"));
    }

    [Fact]
    public void IsanagramRatCarShouldBeFalse()
    {
        Assert.Equal(false, Solution.IsAnagram("rat", "car"));
    }
}
