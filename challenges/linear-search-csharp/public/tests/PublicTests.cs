using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Linearsearch2343ShouldReturnIndex1()
    {
        Assert.Equal(1, Solution.LinearSearch(new int[] { 2, 3, 4 }, 3));
    }

    [Fact]
    public void Linearsearch125ShouldReturnIndex1()
    {
        Assert.Equal(-1, Solution.LinearSearch(new int[] { 1, 2 }, 5));
    }
}
