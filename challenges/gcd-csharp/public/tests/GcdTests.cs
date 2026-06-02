using Challenge;
using Xunit;

namespace Challenge.Tests;

public class GcdTests
{
    [Fact]
    public void Sample()
    {
        Assert.Equal(6, Solution.Gcd(54, 24));
    }

    [Fact]
    public void Coprime()
    {
        Assert.Equal(1, Solution.Gcd(17, 13));
    }
}
