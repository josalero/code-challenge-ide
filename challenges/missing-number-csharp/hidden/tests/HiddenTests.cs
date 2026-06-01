using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Missingnumber964235701ShouldEqual8()
    {
        Assert.Equal(false, Solution.MissingNumber(new int[] { 9, 6, 4, 2, 3, 5, 7, 0, 1 }));
    }

    [Fact]
    public void Missingnumber1ShouldEqual0()
    {
        Assert.Equal(false, Solution.MissingNumber(new int[] { 1 }));
    }
}
