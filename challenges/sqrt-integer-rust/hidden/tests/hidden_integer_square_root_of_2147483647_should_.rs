use challenge::my_sqrt;

#[test]
fn hidden_integer_square_root_of_2147483647_should_() {
    assert_eq!(my_sqrt(2147483647), 46340);
}
