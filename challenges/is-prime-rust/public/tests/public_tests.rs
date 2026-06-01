use challenge::is_prime;

#[test]
fn public_cases() {
    assert_eq!(is_prime(1), false);
    assert_eq!(is_prime(2), true);
    assert_eq!(is_prime(17), true);
}
