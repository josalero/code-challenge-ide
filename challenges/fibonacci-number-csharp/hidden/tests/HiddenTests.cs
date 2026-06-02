using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Fibonacci10ShouldEqual55()
    {
        Assert.Equal(55, Solution.Fib(10));
    }

    [Fact]
    public void Fibonacci6ShouldEqual8()
    {
        Assert.Equal(8, Solution.Fib(6));
    }
}
