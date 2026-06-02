using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Maxprofit241ShouldEqual2()
    {
        Assert.Equal(2, Solution.MaxProfit(new int[] { 2, 4, 1 }));
    }

    [Fact]
    public void Maxprofit12ShouldEqual1()
    {
        Assert.Equal(1, Solution.MaxProfit(new int[] { 1, 2 }));
    }
}
