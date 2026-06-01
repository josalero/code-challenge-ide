using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Gcd4818ShouldEqual12()
    {
        Assert.Equal(12, Solution.Gcd(48, 18));
    }

    [Fact]
    public void Gcd07ShouldEqual7()
    {
        Assert.Equal(7, Solution.Gcd(0, 7));
    }
}
