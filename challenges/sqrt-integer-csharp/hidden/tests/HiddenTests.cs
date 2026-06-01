using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void IntegerSquareRootOf10ShouldBe3()
    {
        Assert.Equal(3, Solution.MySqrt(10));
    }

    [Fact]
    public void IntegerSquareRootOf2147483647ShouldBe4634()
    {
        Assert.Equal(46340, Solution.MySqrt(2147483647));
    }
}
