using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Containsduplicate1231ShouldBeTrue()
    {
        Assert.Equal(true, Solution.ContainsDuplicate(new int[] { 1, 2, 3, 1 }));
    }

    [Fact]
    public void Containsduplicate1234ShouldBeFalse()
    {
        Assert.Equal(false, Solution.ContainsDuplicate(new int[] { 1, 2, 3, 4 }));
    }
}
