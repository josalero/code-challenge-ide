using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{
    [Fact]
    public void Fibonacci0ShouldEqual0()
    {
        Assert.Equal(0, Solution.Fib(0));
    }

    [Fact]
    public void Fibonacci1ShouldEqual1()
    {
        Assert.Equal(1, Solution.Fib(1));
    }

    [Fact]
    public void Fibonacci5ShouldEqual5()
    {
        Assert.Equal(5, Solution.Fib(5));
    }
}
