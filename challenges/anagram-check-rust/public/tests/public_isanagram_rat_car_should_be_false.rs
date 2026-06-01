use challenge::is_anagram;

#[test]
fn public_isanagram_rat_car_should_be_false() {
    assert_eq!(is_anagram("rat", "car"), false);
}
