from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_still_scalar() -> None:
    assert_scalar(3)

