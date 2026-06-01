use challenge::gcd;

#[test]
fn public_gcd_17_13_should_equal_1() {
    assert_eq!(gcd(17, 13), 1);
}
