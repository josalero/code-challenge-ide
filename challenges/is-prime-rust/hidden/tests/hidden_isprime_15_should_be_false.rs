use challenge::is_prime;

#[test]
fn hidden_isprime_15_should_be_false() {
    assert_eq!(is_prime(15), false);
}
