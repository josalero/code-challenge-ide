from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_sales_names() -> None:
    assert_query_result([(2, 'Bob Martinez'), (5, 'Eve Lopez')], ordered=True)

