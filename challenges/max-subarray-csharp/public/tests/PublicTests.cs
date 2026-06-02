using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Maxsubarray213412143ShouldEqual6()
    {
        Assert.Equal(6, Solution.MaxSubArray(new int[] { -2, 1, -3, 4, -1, 2, 1, -4, 3 }));
    }

    [Fact]
    public void Maxsubarray1ShouldEqual1()
    {
        Assert.Equal(1, Solution.MaxSubArray(new int[] { 1 }));
    }
}
