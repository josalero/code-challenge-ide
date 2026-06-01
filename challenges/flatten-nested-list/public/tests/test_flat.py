from solution import *

def test_flat() -> None:
    assert flatten([1, [2, 3], 4]) == [1, 2, 3, 4]

