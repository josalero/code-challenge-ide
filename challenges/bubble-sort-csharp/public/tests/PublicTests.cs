using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Bubblesort312ShouldReturn123()
    {
        Assert.Equal(new int[] { 1, 2, 3 }, Solution.BubbleSort(new int[] { 3, 1, 2 }));
    }

    [Fact]
    public void Bubblesort1ShouldReturn1()
    {
        Assert.Equal(new int[] { 1 }, Solution.BubbleSort(new int[] { 1 }));
    }
}
