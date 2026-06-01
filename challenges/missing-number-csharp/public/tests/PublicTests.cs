using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Missingnumber301ShouldEqual2()
    {
        Assert.Equal(false, Solution.MissingNumber(new int[] { 3, 0, 1 }));
    }

    [Fact]
    public void Missingnumber0ShouldEqual1()
    {
        Assert.Equal(false, Solution.MissingNumber(new int[] { 0 }));
    }
}
