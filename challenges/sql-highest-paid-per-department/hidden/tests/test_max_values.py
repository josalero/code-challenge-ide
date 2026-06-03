from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_max_values() -> None:
    assert_query_result([('Engineering', 120000.00), ('HR', 68000.00), ('Sales', 82000.00)], ordered=True)

