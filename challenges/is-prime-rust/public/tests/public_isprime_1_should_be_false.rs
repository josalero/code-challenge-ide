use challenge::is_prime;

#[test]
fn public_isprime_1_should_be_false() {
    assert_eq!(is_prime(1), false);
}
