use challenge::gcd;

#[test]
fn public_cases() {
    assert_eq!(gcd(54, 24), 6);
    assert_eq!(gcd(17, 13), 1);
    assert_eq!(gcd(25, 15), 5);
}
