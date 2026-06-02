using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Mergesorted1ShouldReturn1()
    {
        Assert.Equal(new int[] { 1 }, Solution.MergeSorted(new int[] { 1 }, new int[] {  }));
    }

    [Fact]
    public void Mergesorted111ShouldReturn111()
    {
        Assert.Equal(new int[] { 1, 1, 1 }, Solution.MergeSorted(new int[] { 1, 1 }, new int[] { 1 }));
    }
}
