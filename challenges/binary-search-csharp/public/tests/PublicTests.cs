using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Binarysearch135793ShouldReturnIndex1()
    {
        Assert.Equal(1, Solution.BinarySearch(new int[] { 1, 3, 5, 7, 9 }, 3));
    }

    [Fact]
    public void Binarysearch135794ShouldReturnIndex1()
    {
        Assert.Equal(-1, Solution.BinarySearch(new int[] { 1, 3, 5, 7, 9 }, 4));
    }
}
