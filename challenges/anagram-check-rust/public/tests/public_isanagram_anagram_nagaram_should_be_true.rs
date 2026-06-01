use challenge::is_anagram;

#[test]
fn public_isanagram_anagram_nagaram_should_be_true() {
    assert_eq!(is_anagram("anagram", "nagaram"), true);
}
