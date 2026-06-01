from solution import *

def test_negative() -> None:
    assert abs(celsius_to_fahrenheit(-40) - (-40.0)) < 0.001

