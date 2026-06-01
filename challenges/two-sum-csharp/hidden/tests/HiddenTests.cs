using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Twosum336ShouldReturn01()
    {
        Assert.Equal(new int[] { 0, 1 }, Solution.TwoSum(new int[] { 3, 3 }, 6));
    }

    [Fact]
    public void Twosum1235ShouldReturn12()
    {
        Assert.Equal(new int[] { 1, 2 }, Solution.TwoSum(new int[] { -1, -2, -3 }, -5));
    }
}
