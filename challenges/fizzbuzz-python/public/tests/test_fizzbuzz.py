from solution import fizz_buzz


def test_fizz_buzz_for_one() -> None:
    assert fizz_buzz(1) == ["1"]


def test_fizz_buzz_for_five() -> None:
    assert fizz_buzz(5) == ["1", "2", "Fizz", "4", "Buzz"]


def test_fizz_buzz_includes_fizzbuzz_at_fifteen() -> None:
    result = fizz_buzz(15)
    assert result[14] == "FizzBuzz"
