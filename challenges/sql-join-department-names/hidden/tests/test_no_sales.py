from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_no_sales() -> None:
    assert_query_result([('Alice Chen', 'Engineering'), ('Carol White', 'Engineering'), ('Frank Ng', 'Engineering')], ordered=True)

