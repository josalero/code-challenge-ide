using Challenge;
using Xunit;

namespace Challenge.Tests;

public class GcdHiddenTests
{
    [Fact]
    public void Zero()
    {
        Assert.Equal(7, Solution.Gcd(0, 7));
    }

    [Fact]
    public void Equal()
    {
        Assert.Equal(12, Solution.Gcd(12, 12));
    }
}
