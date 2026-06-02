use challenge::max_sub_array;

#[test]
fn hidden_maxsubarray_5_4_1_7_8_should_equal_23() {
    assert_eq!(max_sub_array(&[5, 4, -1, 7, 8]), 23);
}
