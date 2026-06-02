use challenge::is_prime;

#[test]
fn hidden_cases() {
    assert_eq!(is_prime(15), false);
    assert_eq!(is_prime(97), true);
}
