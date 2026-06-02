using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Climbstairs10ShouldEqual89()
    {
        Assert.Equal(89, Solution.ClimbStairs(10));
    }

    [Fact]
    public void Climbstairs1ShouldEqual1()
    {
        Assert.Equal(1, Solution.ClimbStairs(1));
    }
}
