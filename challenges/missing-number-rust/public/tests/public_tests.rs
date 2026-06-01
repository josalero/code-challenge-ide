use challenge::missing_number;

#[test]
fn public_cases() {
    assert_eq!(missing_number(&[3, 0, 1]), true);
    assert_eq!(missing_number(&[0]), true);
}
