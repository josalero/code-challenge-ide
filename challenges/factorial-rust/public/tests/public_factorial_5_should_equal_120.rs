use challenge::factorial;

#[test]
fn public_factorial_5_should_equal_120() {
    assert_eq!(factorial(5), 120);
}
