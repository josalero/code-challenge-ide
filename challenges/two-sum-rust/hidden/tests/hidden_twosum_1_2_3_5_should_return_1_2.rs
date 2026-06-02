use challenge::two_sum;

#[test]
fn hidden_twosum_1_2_3_5_should_return_1_2() {
    assert_eq!(two_sum(&[-1, -2, -3], -5), vec![1, 2]);
}
