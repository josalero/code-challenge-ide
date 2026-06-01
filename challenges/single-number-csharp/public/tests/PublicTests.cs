using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Singlenumber221ShouldEqual1()
    {
        Assert.Equal(1, Solution.SingleNumber(new int[] { 2, 2, 1 }));
    }

    [Fact]
    public void Singlenumber41212ShouldEqual4()
    {
        Assert.Equal(4, Solution.SingleNumber(new int[] { 4, 1, 2, 1, 2 }));
    }
}
