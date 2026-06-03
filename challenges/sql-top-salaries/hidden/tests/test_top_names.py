from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_top_names() -> None:
    assert_query_result([('Alice Chen',), ('Carol White',), ('Frank Ng',)], ordered=True)

