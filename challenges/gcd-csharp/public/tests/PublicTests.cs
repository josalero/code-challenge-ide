using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Gcd5424ShouldEqual6()
    {
        Assert.Equal(6, Solution.Gcd(54, 24));
    }

    [Fact]
    public void Gcd1713ShouldEqual1()
    {
        Assert.Equal(1, Solution.Gcd(17, 13));
    }

    [Fact]
    public void Gcd2515ShouldEqual5()
    {
        Assert.Equal(5, Solution.Gcd(25, 15));
    }
}
