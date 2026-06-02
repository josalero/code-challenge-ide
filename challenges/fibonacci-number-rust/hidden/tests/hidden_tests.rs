use challenge::fib;

#[test]
fn hidden_cases() {
    assert_eq!(fib(10), 55);
    assert_eq!(fib(6), 8);
}
