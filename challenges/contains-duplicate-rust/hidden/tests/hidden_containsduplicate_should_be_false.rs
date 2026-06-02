use challenge::contains_duplicate;

#[test]
fn hidden_containsduplicate_should_be_false() {
    assert_eq!(contains_duplicate(&[]), false);
}
