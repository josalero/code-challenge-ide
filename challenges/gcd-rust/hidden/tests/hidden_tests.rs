use challenge::gcd;

#[test]
fn hidden_cases() {
    assert_eq!(gcd(48, 18), 12);
    assert_eq!(gcd(0, 7), 7);
}
