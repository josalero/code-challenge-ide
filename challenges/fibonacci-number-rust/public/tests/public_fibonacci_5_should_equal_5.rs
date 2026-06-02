use challenge::fib;

#[test]
fn public_fibonacci_5_should_equal_5() {
    assert_eq!(fib(5), 5);
}
