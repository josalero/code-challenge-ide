use challenge::factorial;

#[test]
fn public_factorial_0_should_equal_1() {
    assert_eq!(factorial(0), 1);
}
