using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Ispoweroftwo1ShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsPowerOfTwo(1));
    }

    [Fact]
    public void Ispoweroftwo3ShouldBeFalse()
    {
        Assert.Equal(false, Solution.IsPowerOfTwo(3));
    }
}
