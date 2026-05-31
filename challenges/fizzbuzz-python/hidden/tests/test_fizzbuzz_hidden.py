from solution import fizz_buzz


def test_fizz_buzz_length_matches_n() -> None:
    assert len(fizz_buzz(30)) == 30


def test_fizz_buzz_third_element_is_fizz() -> None:
    assert fizz_buzz(10)[2] == "Fizz"
