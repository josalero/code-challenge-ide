using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Climbstairs2ShouldEqual2()
    {
        Assert.Equal(2, Solution.ClimbStairs(2));
    }

    [Fact]
    public void Climbstairs3ShouldEqual3()
    {
        Assert.Equal(3, Solution.ClimbStairs(3));
    }
}
