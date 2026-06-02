use challenge::contains_duplicate;

#[test]
fn public_cases() {
    assert_eq!(contains_duplicate(&[1, 2, 3, 1]), true);
    assert_eq!(contains_duplicate(&[1, 2, 3, 4]), false);
}
