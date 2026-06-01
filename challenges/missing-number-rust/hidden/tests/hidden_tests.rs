use challenge::missing_number;

#[test]
fn hidden_cases() {
    assert_eq!(missing_number(&[9, 6, 4, 2, 3, 5, 7, 0, 1]), true);
    assert_eq!(missing_number(&[1]), false);
}
