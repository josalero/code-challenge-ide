using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Isprime15ShouldBeFalse()
    {
        Assert.Equal(false, Solution.IsPrime(15));
    }

    [Fact]
    public void Isprime97ShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsPrime(97));
    }
}
