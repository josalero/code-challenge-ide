use challenge::is_valid_parentheses;

#[test]
fn public_isvalid_should_be_true() {
    assert_eq!(is_valid_parentheses("()[]{}"), true);
}
