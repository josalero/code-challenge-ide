using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Maxsubarray54178ShouldEqual23()
    {
        Assert.Equal(23, Solution.MaxSubArray(new int[] { 5, 4, -1, 7, 8 }));
    }

    [Fact]
    public void Maxsubarray1ShouldEqual1()
    {
        Assert.Equal(-1, Solution.MaxSubArray(new int[] { -1 }));
    }
}
