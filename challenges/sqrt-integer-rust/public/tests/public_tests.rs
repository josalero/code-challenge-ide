use challenge::my_sqrt;

#[test]
fn public_cases() {
    assert_eq!(my_sqrt(8), 2);
    assert_eq!(my_sqrt(0), 0);
}
