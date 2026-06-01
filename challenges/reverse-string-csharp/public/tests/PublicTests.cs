using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void ReversestringHelloShouldBeOlleh()
    {
        Assert.Equal("olleh", Solution.ReverseString("hello"));
    }

    [Fact]
    public void ReversestringShouldBe()
    {
        Assert.Equal("", Solution.ReverseString(""));
    }
}
