use challenge::single_number;

#[test]
fn public_singlenumber_4_1_2_1_2_should_equal_4() {
    assert_eq!(single_number(&[4, 1, 2, 1, 2]), 4);
}
