using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Containsduplicate11ShouldBeTrue()
    {
        Assert.Equal(true, Solution.ContainsDuplicate(new int[] { 1, 1 }));
    }

    [Fact]
    public void ContainsduplicateShouldBeFalse()
    {
        Assert.Equal(false, Solution.ContainsDuplicate(new int[] {  }));
    }
}
