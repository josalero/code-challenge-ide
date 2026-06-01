from solution import *

def test_missing() -> None:
    assert binary_search([-1, 0, 3, 5, 9], 2) == -1

