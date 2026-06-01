use challenge::gcd;

#[test]
fn public_gcd_25_15_should_equal_5() {
    assert_eq!(gcd(25, 15), 5);
}
