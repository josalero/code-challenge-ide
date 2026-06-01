using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void BubblesortShouldReturn()
    {
        Assert.Equal(new int[] {  }, Solution.BubbleSort(new int[] {  }));
    }

    [Fact]
    public void Bubblesort54321ShouldReturn12345()
    {
        Assert.Equal(new int[] { 1, 2, 3, 4, 5 }, Solution.BubbleSort(new int[] { 5, 4, 3, 2, 1 }));
    }
}
