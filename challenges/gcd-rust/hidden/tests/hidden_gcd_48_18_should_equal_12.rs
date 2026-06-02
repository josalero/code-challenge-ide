use challenge::gcd;

#[test]
fn hidden_gcd_48_18_should_equal_12() {
    assert_eq!(gcd(48, 18), 12);
}
