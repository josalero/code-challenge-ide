using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Singlenumber1ShouldEqual1()
    {
        Assert.Equal(1, Solution.SingleNumber(new int[] { 1 }));
    }

    [Fact]
    public void Singlenumber636ShouldEqual3()
    {
        Assert.Equal(3, Solution.SingleNumber(new int[] { 6, 3, 6 }));
    }
}
