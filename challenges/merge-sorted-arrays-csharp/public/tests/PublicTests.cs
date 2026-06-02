using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Mergesorted123256ShouldReturn12235()
    {
        Assert.Equal(new int[] { 1, 2, 2, 3, 5, 6 }, Solution.MergeSorted(new int[] { 1, 2, 3 }, new int[] { 2, 5, 6 }));
    }

    [Fact]
    public void Mergesorted1ShouldReturn1()
    {
        Assert.Equal(new int[] { 1 }, Solution.MergeSorted(new int[] {  }, new int[] { 1 }));
    }
}
