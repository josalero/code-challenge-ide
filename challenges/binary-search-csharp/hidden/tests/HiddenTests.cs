using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Binarysearch2462ShouldReturnIndex0()
    {
        Assert.Equal(0, Solution.BinarySearch(new int[] { 2, 4, 6 }, 2));
    }

    [Fact]
    public void Binarysearch1ShouldReturnIndex1()
    {
        Assert.Equal(-1, Solution.BinarySearch(new int[] {  }, 1));
    }
}
