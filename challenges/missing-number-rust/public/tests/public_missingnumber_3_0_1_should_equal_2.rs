use challenge::missing_number;

#[test]
fn public_missingnumber_3_0_1_should_equal_2() {
    assert_eq!(missing_number(&[3, 0, 1]), true);
}
