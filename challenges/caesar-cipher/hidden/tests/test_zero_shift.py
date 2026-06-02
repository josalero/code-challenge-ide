from solution import *

def test_zero_shift() -> None:
    assert caesar_cipher("hello", 0) == "hello"

