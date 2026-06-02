use challenge::two_sum;

#[test]
fn hidden_twosum_3_3_6_should_return_0_1() {
    assert_eq!(two_sum(&[3, 3], 6), vec![0, 1]);
}
