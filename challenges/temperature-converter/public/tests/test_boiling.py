from solution import *

def test_boiling() -> None:
    assert abs(celsius_to_fahrenheit(100) - 212.0) < 0.001

