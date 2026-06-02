use challenge::is_prime;

#[test]
fn public_isprime_17_should_be_true() {
    assert_eq!(is_prime(17), true);
}
