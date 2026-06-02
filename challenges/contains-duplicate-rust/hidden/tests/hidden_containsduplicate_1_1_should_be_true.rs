use challenge::contains_duplicate;

#[test]
fn hidden_containsduplicate_1_1_should_be_true() {
    assert_eq!(contains_duplicate(&[1, 1]), true);
}
