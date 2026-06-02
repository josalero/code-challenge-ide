use challenge::two_sum;

#[test]
fn public_twosum_3_2_4_6_should_return_1_2() {
    assert_eq!(two_sum(&[3, 2, 4], 6), vec![1, 2]);
}
