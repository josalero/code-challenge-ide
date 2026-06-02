use challenge::gcd;

#[test]
fn public_gcd_54_24_should_equal_6() {
    assert_eq!(gcd(54, 24), 6);
}
