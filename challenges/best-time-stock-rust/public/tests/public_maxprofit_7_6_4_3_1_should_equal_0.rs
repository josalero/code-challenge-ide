use challenge::max_profit;

#[test]
fn public_maxprofit_7_6_4_3_1_should_equal_0() {
    assert_eq!(max_profit(&[7, 6, 4, 3, 1]), 0);
}
