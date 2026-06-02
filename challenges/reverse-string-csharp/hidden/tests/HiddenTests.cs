using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void ReversestringAShouldBeA()
    {
        Assert.Equal("a", Solution.ReverseString("a"));
    }

    [Fact]
    public void ReversestringRaceShouldBeEcar()
    {
        Assert.Equal("ecar", Solution.ReverseString("race"));
    }
}
