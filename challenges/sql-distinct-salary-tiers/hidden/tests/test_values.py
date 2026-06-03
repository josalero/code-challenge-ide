from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_values() -> None:
    assert_query_result([(120000.00,), (105000.00,), (99000.00,), (82000.00,)], ordered=True)

