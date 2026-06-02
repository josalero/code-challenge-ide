use challenge::factorial;

#[test]
fn hidden_cases() {
    assert_eq!(factorial(10), 3628800);
    assert_eq!(factorial(3), 6);
}
