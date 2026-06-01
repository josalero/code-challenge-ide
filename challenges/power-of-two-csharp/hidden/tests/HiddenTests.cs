using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Ispoweroftwo16ShouldBeTrue()
    {
        Assert.Equal(true, Solution.IsPowerOfTwo(16));
    }

    [Fact]
    public void Ispoweroftwo0ShouldBeFalse()
    {
        Assert.Equal(false, Solution.IsPowerOfTwo(0));
    }
}
