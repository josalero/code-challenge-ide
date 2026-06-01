use challenge::max_sub_array;

#[test]
fn public_maxsubarray_2_1_3_4_1_2_1_4_3_should_equa() {
    assert_eq!(max_sub_array(&[-2, 1, -3, 4, -1, 2, 1, -4, 3]), 6);
}
