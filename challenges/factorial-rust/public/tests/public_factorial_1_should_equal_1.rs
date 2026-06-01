use challenge::factorial;

#[test]
fn public_factorial_1_should_equal_1() {
    assert_eq!(factorial(1), 1);
}
