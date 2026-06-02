use challenge::my_sqrt;

#[test]
fn public_integer_square_root_of_0_should_be_0() {
    assert_eq!(my_sqrt(0), 0);
}
