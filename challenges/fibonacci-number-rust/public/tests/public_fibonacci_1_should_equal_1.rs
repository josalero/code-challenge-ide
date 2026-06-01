use challenge::fib;

#[test]
fn public_fibonacci_1_should_equal_1() {
    assert_eq!(fib(1), 1);
}
