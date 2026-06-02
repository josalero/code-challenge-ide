use challenge::gcd;

#[test]
fn hidden_gcd_0_7_should_equal_7() {
    assert_eq!(gcd(0, 7), 7);
}
