from solution import *

def test_body() -> None:
    assert abs(celsius_to_fahrenheit(37) - 98.6) < 0.2

