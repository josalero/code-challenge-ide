using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Factorial10ShouldEqual3628800()
    {
        Assert.Equal(3628800, Solution.Factorial(10));
    }

    [Fact]
    public void Factorial3ShouldEqual6()
    {
        Assert.Equal(6, Solution.Factorial(3));
    }
}
