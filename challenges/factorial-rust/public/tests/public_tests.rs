use challenge::factorial;

#[test]
fn public_cases() {
    assert_eq!(factorial(0), 1);
    assert_eq!(factorial(5), 120);
    assert_eq!(factorial(1), 1);
}
