from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_rows() -> None:
    assert_query_result([(1, 'Alice Chen'), (3, 'Carol White')], ordered=True)

