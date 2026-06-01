use challenge::max_profit;

#[test]
fn public_maxprofit_7_1_5_3_6_4_should_equal_5() {
    assert_eq!(max_profit(&[7, 1, 5, 3, 6, 4]), 5);
}
