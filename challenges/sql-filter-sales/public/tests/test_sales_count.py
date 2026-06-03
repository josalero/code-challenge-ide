from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_sales_count() -> None:
    assert_row_count(2)

