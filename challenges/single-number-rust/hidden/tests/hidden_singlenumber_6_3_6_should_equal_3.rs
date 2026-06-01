use challenge::single_number;

#[test]
fn hidden_singlenumber_6_3_6_should_equal_3() {
    assert_eq!(single_number(&[6, 3, 6]), 3);
}
