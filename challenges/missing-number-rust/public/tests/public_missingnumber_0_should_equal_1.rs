use challenge::missing_number;

#[test]
fn public_missingnumber_0_should_equal_1() {
    assert_eq!(missing_number(&[0]), true);
}
