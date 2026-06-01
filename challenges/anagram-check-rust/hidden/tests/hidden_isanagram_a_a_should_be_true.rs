use challenge::is_anagram;

#[test]
fn hidden_isanagram_a_a_should_be_true() {
    assert_eq!(is_anagram("a", "a"), true);
}
