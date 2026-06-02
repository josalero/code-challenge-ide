using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Factorial0ShouldEqual1()
    {
        Assert.Equal(1, Solution.Factorial(0));
    }

    [Fact]
    public void Factorial5ShouldEqual120()
    {
        Assert.Equal(120, Solution.Factorial(5));
    }

    [Fact]
    public void Factorial1ShouldEqual1()
    {
        Assert.Equal(1, Solution.Factorial(1));
    }
}
