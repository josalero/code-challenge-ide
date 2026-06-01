using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Isprime1ShouldBeFalse()
    {
        Assert.Equal(false, Solution.IsPrime(1));
    }

    [Fact]
    public void Isprime2ShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsPrime(2));
    }

    [Fact]
    public void Isprime17ShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsPrime(17));
    }
}
