use challenge::is_anagram;

#[test]
fn hidden_isanagram_ab_a_should_be_false() {
    assert_eq!(is_anagram("ab", "a"), false);
}
