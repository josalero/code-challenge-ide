from ctl_sql import assert_query_result, assert_scalar, assert_row_count

def test_avg_values() -> None:
    assert_query_result([('Engineering', 108000.00), ('HR', 68000.00), ('Sales', 78500.00)], ordered=True)

