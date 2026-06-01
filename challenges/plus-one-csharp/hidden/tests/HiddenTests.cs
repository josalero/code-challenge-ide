using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Plusone0ShouldReturn1()
    {
        Assert.Equal(new int[] { 1 }, Solution.PlusOne(new int[] { 0 }));
    }

    [Fact]
    public void Plusone999ShouldReturn1000()
    {
        Assert.Equal(new int[] { 1, 0, 0, 0 }, Solution.PlusOne(new int[] { 9, 9, 9 }));
    }
}
