from solution import *

def test_preserve_space() -> None:
    assert caesar_cipher("a b", 1) == "b c"

