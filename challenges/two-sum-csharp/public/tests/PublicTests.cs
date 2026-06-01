using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Twosum2711159ShouldReturn01()
    {
        Assert.Equal(new int[] { 0, 1 }, Solution.TwoSum(new int[] { 2, 7, 11, 15 }, 9));
    }

    [Fact]
    public void Twosum3246ShouldReturn12()
    {
        Assert.Equal(new int[] { 1, 2 }, Solution.TwoSum(new int[] { 3, 2, 4 }, 6));
    }
}
