use challenge::is_valid_parentheses;

#[test]
fn hidden_isvalid_should_be_false() {
    assert_eq!(is_valid_parentheses("(]"), false);
}
