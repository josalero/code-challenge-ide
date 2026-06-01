use challenge::contains_duplicate;

#[test]
fn hidden_cases() {
    assert_eq!(contains_duplicate(&[1, 1]), true);
    assert_eq!(contains_duplicate(&[]), false);
}
