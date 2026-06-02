use challenge::fib;

#[test]
fn public_fibonacci_0_should_equal_0() {
    assert_eq!(fib(0), 0);
}
