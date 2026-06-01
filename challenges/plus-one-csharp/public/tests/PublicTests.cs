using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Plusone123ShouldReturn124()
    {
        Assert.Equal(new int[] { 1, 2, 4 }, Solution.PlusOne(new int[] { 1, 2, 3 }));
    }

    [Fact]
    public void Plusone9ShouldReturn10()
    {
        Assert.Equal(new int[] { 1, 0 }, Solution.PlusOne(new int[] { 9 }));
    }
}
