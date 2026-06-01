use challenge::fib;

#[test]
fn public_cases() {
    assert_eq!(fib(0), 0);
    assert_eq!(fib(1), 1);
    assert_eq!(fib(5), 5);
}
