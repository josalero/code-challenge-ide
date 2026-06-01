use challenge::two_sum;

#[test]
fn public_twosum_2_7_11_15_9_should_return_0_1() {
    assert_eq!(two_sum(&[2, 7, 11, 15], 9), vec![0, 1]);
}
