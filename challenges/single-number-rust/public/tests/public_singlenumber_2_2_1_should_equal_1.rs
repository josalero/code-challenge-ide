use challenge::single_number;

#[test]
fn public_singlenumber_2_2_1_should_equal_1() {
    assert_eq!(single_number(&[2, 2, 1]), 1);
}
