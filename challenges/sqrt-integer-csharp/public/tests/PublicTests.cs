using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void IntegerSquareRootOf8ShouldBe2()
    {
        Assert.Equal(2, Solution.MySqrt(8));
    }

    [Fact]
    public void IntegerSquareRootOf0ShouldBe0()
    {
        Assert.Equal(0, Solution.MySqrt(0));
    }
}
