using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Linearsearch99ShouldReturnIndex0()
    {
        Assert.Equal(0, Solution.LinearSearch(new int[] { 9 }, 9));
    }

    [Fact]
    public void Linearsearch1ShouldReturnIndex1()
    {
        Assert.Equal(-1, Solution.LinearSearch(new int[] {  }, 1));
    }
}
