using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Maxprofit715364ShouldEqual5()
    {
        Assert.Equal(5, Solution.MaxProfit(new int[] { 7, 1, 5, 3, 6, 4 }));
    }

    [Fact]
    public void Maxprofit76431ShouldEqual0()
    {
        Assert.Equal(0, Solution.MaxProfit(new int[] { 7, 6, 4, 3, 1 }));
    }
}
