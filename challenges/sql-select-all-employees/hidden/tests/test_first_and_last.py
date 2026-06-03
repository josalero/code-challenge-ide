from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_first_and_last() -> None:
    assert_query_result([(1, 'Alice Chen'), (6, 'Frank Ng')], ordered=True)

